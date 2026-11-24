package com.gen.auth.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gen.auth.apiexception.ApiException;
import com.gen.auth.apiexception.ErrorCodes;
import com.gen.auth.helperutils.ObjectUtils;
import com.gen.auth.security.helperDTOs.LoginRefreshRequest;
import com.gen.auth.security.helperDTOs.LoginRequest;
import com.gen.auth.helperutils.RedisStore;
import com.gen.auth.dto.UserDto;
import com.gen.auth.helperutils.Constants;
import com.gen.auth.security.helperDTOs.LoginResponse;
import com.gen.auth.model.RefreshToken;
import com.gen.auth.model.User;
import com.gen.auth.repository.RefreshTokenRepository;
import com.gen.auth.repository.UserRepository;
import com.gen.auth.security.CookieService;
import com.gen.auth.security.JWTService;
import com.gen.auth.security.helperDTOs.LogoutRequest;
import com.gen.auth.service.AuthService;
import com.gen.auth.service.UserService;
import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final RedisStore redisStore;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper;

    @Override
    public UserDto register(UserDto userDto) {
        return userService.createUser(userDto);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {

        User user = null;
        Set<String> authorities = new HashSet<>();
        try {
             user = userService.getUserByEmail(loginRequest.getEmail());
        } catch (Exception e) {
            logger.error("Failed to get user by email", e);
            throw new ApiException("Invalid Email Id or Password", HttpStatus.BAD_REQUEST, new IllegalStateException("Invalid Email Id or Password")).addErrors(ErrorCodes.INVALID_EMAIL_ID_OR_PASSWORD);
        }

//        User user = optionalUser.orElseThrow(() -> new ApiException("Invalid Email Id or Password", HttpStatus.BAD_REQUEST,new IllegalStateException("Invalid Email Id or Password")).addErrors(ErrorCodes.INVALID_EMAIL_ID_OR_PASSWORD));

        boolean matches = passwordEncoder.matches(loginRequest.getPassword(),user.getPassword());

        if(!matches){
            throw new ApiException("Invalid Email Id or Password", HttpStatus.BAD_REQUEST,new IllegalStateException("Invalid Email Id or Password")).addErrors(ErrorCodes.INVALID_EMAIL_ID_OR_PASSWORD);
        }

        if(!"active".equalsIgnoreCase(user.getStatus().name())){
            throw new ApiException("User is not Active",HttpStatus.BAD_REQUEST,new IllegalStateException("Invalid Email Id or Password")).addErrors(ErrorCodes.USER_IS_INACTIVE);
        }


        String uuid = UUID.randomUUID().toString();
        String authToken = jwtService.generateJwtToken(user,uuid);
        String refreshToken = jwtService.generateRefreshToken(authToken);

        createOrUpdateRefreshToken(uuid,refreshToken,user);

        cookieService.addNoStore(response);
        cookieService.attachCookie(response,refreshToken,(int) jwtService.getRefreshTtlSeconds());

        LoginResponse loginResponse =  new LoginResponse(user.getId(),authToken,refreshToken);

        try{
            redisStore.set(Constants.TOKEN_KEY+user.getOrganizationId()+"-"+user.getId()+"-"+uuid,authToken);
        }catch (Exception e){
            logger.error("Failed to set token in Redis", e);
        }
        if(ObjectUtils.isNullOrEmpty(user.getUserRoles())){
           user.setRoles(user.getRoles().stream().map(e -> "ROLE_" + e).collect(Collectors.toSet()));
        }
        try {
            authorities.addAll(user.getRoles().stream().map(role -> "ROLE_" + role).collect(Collectors.toSet()));
            user.getPermissions().forEach(permission -> authorities.add(permission.name()));
            redisStore.set(Constants.AUTHORITIES+user.getOrganizationId()+"-"+user.getId(),authorities.toString());
        }catch (Exception e){
            logger.error("Failed to set user in Redis", e);
        }

        return loginResponse;
    }

    public LoginResponse refreshAccessToken(LoginRefreshRequest loginRefreshRequest,HttpServletResponse response, HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromRequestAndResponse(request,response,loginRefreshRequest,null).orElseThrow(() -> new ApiException("Refresh token is required",HttpStatus.BAD_REQUEST,new BadCredentialsException("Refresh token is required")).addErrors(ErrorCodes.REFRESH_TOKEN_REQUIRED));

        // 1. Validate that it is actually a refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new ApiException("Invalid refresh token",HttpStatus.BAD_REQUEST,new BadCredentialsException("Invalid refresh token")).addErrors(ErrorCodes.INVALID_REFRESH_TOKEN);
        }

        // 2. Extract claims
        Claims claims = jwtService.parseToken(refreshToken);

        long userId = jwtService.extractUserId(refreshToken);

        String username = jwtService.extractEmailId(refreshToken);

        // 3. Get the ORIGINAL refresh expiry
        Instant refreshExpiry = jwtService.getRefreshExpiry(refreshToken);

        // 4. Check refresh session expiration
        if (Instant.now().isAfter(refreshExpiry)) {
            throw new ApiException( "Refresh token expired. Please login again.",HttpStatus.UNAUTHORIZED,new IllegalStateException("Refresh token expired. Please login again."));
        }

        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(claims.getId()).orElseThrow(() -> new ApiException("Refresh token not found",HttpStatus.BAD_REQUEST,new BadCredentialsException("Refresh token not found")).addErrors(ErrorCodes.REFRESH_TOKEN_NOT_FOUND));
        if(storedRefreshToken.isRevoked()){
            throw new ApiException("Refresh token is expired or revoked. Please login again.",HttpStatus.UNAUTHORIZED,new BadCredentialsException("Refresh token is revoked")).addErrors(ErrorCodes.REFRESH_TOKEN_REVOKED);
        }

        // 5. Generate NEW access token
        //
        // IMPORTANT:
        // We pass the ORIGINAL refresh expiry.
        //
        // Therefore the new access token cannot
        // extend the refresh session.
        String newAccessToken = jwtService.refreshJwtToken(refreshToken);

        try{
            User user = userRepository.findByEmailId(username).orElseThrow(() -> new ApiException("User not found",HttpStatus.BAD_REQUEST,new BadCredentialsException("User not found")).addErrors(ErrorCodes.USER_NOT_FOUND));
            createOrUpdateRefreshToken(claims.getId(),newAccessToken,user);
        } catch (Exception e) {
            logger.error("error in getting user :: "+e.getCause());
        }

        // 6. Return only a new access token
        //
        // The original refresh token is still valid
        // until its original expiry.
        cookieService.addNoStore(response);
        cookieService.attachCookie(response,refreshToken,(int) jwtService.getRefreshTtlSeconds());
        return new LoginResponse(userId,newAccessToken,null);
    }

    public void createOrUpdateRefreshToken(String uuid, String refreshToken,User user){
        RefreshToken refreshToken1 = null;

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(user.getId());
        if(optionalRefreshToken.isPresent()){
            refreshToken1 = optionalRefreshToken.get();
            refreshToken1.setJti(uuid);
            refreshToken1.setExpiryDate(jwtService.parseToken(refreshToken).getExpiration().toInstant());
            refreshToken1.setRevoked(false);
        }else {
            refreshToken1 = RefreshToken.builder()
                    .userId(user.getId())
                    .jti(uuid)
                    .expiryDate(jwtService.parseToken(refreshToken).getExpiration().toInstant())
                    .isRevoked(false)
                    .build();
        }
        refreshTokenRepository.save(refreshToken1);
    }

    public Optional<String> getRefreshTokenFromRequestAndResponse(HttpServletRequest request, HttpServletResponse response,LoginRefreshRequest loginRefreshRequest,LogoutRequest logoutRequest) {

        //1. Get the refresh token from the request cookie
        if(request.getCookies() != null){
            Optional<String> refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equalsIgnoreCase(cookieService.getCookieName()))
                    .map(Cookie::getValue)
                    .filter(v-> !v.isBlank())
                    .findFirst();

            if(refreshToken.isPresent())
                return refreshToken;
        }

        //2 get the refresh token from custom header
        String refreshToken = request.getHeader("X-Refresh-Token");
        if(!Strings.isNullOrEmpty(refreshToken))
            return Optional.of(refreshToken);

        //3. Authorization header
        String authHeader = request.getHeader("Authorization");
        if(!Strings.isNullOrEmpty(authHeader) && authHeader.startsWith("Bearer ")){
            return Optional.of(authHeader.substring(7));
        }

        if(loginRefreshRequest != null && !Strings.isNullOrEmpty(loginRefreshRequest.refreshToken())){
            return Optional.of(loginRefreshRequest.refreshToken());
        }

        if(logoutRequest != null && !Strings.isNullOrEmpty(logoutRequest.refreshToken())){
            return Optional.of(logoutRequest.refreshToken());
        }

//        //4. get refresh token from response cookie
//        return response.getHeaders(cookieService.getCookieName()).stream().findFirst();

        return Optional.empty();
    }

    @Override
    public void logout(LogoutRequest logoutRequest, HttpServletResponse response, HttpServletRequest request) {
        Optional<String> refreshToken = getRefreshTokenFromRequestAndResponse(request, response,null, logoutRequest);
        if (refreshToken.isPresent()) {
            String token = refreshToken.get();
            Claims claims = jwtService.parseToken(token);
            String jti = claims.getId();

            Optional<RefreshToken> storedToken = refreshTokenRepository.findByJti(jti);
            if (storedToken.isPresent()) {
                RefreshToken rt = storedToken.get();
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            }

            try {
                String username = jwtService.extractEmailId(token);
                Optional<User> user = userRepository.findByEmailId(username);
                if (user.isPresent()) {
                    redisStore.delete(Constants.TOKEN_KEY + user.get().getOrganizationId() + "-" + user.get().getId() + "-" + jti);
                }
            } catch (Exception e) {
                logger.error("Error during logout: ", e);
            }
        }else {
            logger.error("No refresh token found in request");
        }

        cookieService.clearCookie(response);
    }
}

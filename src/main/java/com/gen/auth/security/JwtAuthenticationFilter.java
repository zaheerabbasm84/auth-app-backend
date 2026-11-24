package com.gen.auth.security;

import com.gen.auth.apiexception.ApiException;
import com.gen.auth.apiexception.ErrorCodes;
import com.gen.auth.helperutils.Constants;
import com.gen.auth.helperutils.RedisStore;
import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final RedisStore redisStore;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer")){
            String token = header.substring(7);
            try{
                if(jwtService.isAccessToken(token)){
                    Claims claims = jwtService.parseToken(token);
                    long userId = ((Number) claims.get("userId")).longValue();
                    String emailId = claims.get("emailId",String.class);
                    String organizationId = Objects.toString(claims.get("orgId"),null);
                    if(Strings.isNullOrEmpty(organizationId)){
                        throw new ApiException("Invalid authorization token", HttpStatus.BAD_REQUEST).addErrors(ErrorCodes.INVALID_AUTHORIZATION_TOKEN);
                    }

                    String redisToken = redisStore.get(Constants.TOKEN_KEY+organizationId+"-"+userId+"-"+claims.get("jti",String.class));
                    if(Strings.isNullOrEmpty(redisToken)){
                        throw new ApiException("User is Unauthorized", HttpStatus.UNAUTHORIZED,new IllegalStateException("User is Unauthorized")).addErrors(ErrorCodes.USER_IS_UNAUTHORIZED);
                    }

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(emailId,null,null);

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    if(SecurityContextHolder.getContext().getAuthentication() == null){
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                    filterChain.doFilter(request,response);
                }
                else {
                    filterChain.doFilter(request,response);
                }
            }catch (ExpiredJwtException e) {
//                request.setAttribute("errorCode",ErrorCodes.EXPIRED_AUTHORIZATION_TOKEN);
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException(
                                "Authorization token has expired", e
                        )
                );

            } catch (MalformedJwtException e) {
//                request.setAttribute("errorCode",ErrorCodes.MALFORMED_AUTHORIZATION_TOKEN);
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException(
                                "Malformed authorization token", e
                        )
                );

            } catch (JwtException e) {
//                request.setAttribute("errorCode",ErrorCodes.INVALID_AUTHORIZATION_TOKEN);
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException(
                                "Invalid authorization token", e
                        )
                );
            }catch (Exception e) {
                if(e instanceof ApiException){
                    Object errorCodes = ((ApiException) e).getErrors().get(0);
                    if(errorCodes instanceof ErrorCodes){
                        request.setAttribute("errorCode",errorCodes);
                    }
                }
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException(
                                e.getMessage(), e
                        )
                );
            }
        }else{
            filterChain.doFilter(request,response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();

        return uri.startsWith("/api/v1/auth/login")
                || uri.startsWith("/api/v1/auth/register")
                || uri.startsWith("/api/v1/auth/refreshToken")
                || uri.startsWith("/api/v1/auth/logout");
    }
}

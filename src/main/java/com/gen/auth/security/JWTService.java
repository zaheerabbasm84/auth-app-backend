package com.gen.auth.security;

import com.gen.auth.apiexception.ApiException;
import com.gen.auth.model.Permissions;
import com.gen.auth.model.User;
import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;


@Service
@Getter
@Setter
public class JWTService {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final  long refreshTtlSeconds;
    private final String issuer;

    public JWTService(@Value("${security.jwt.secret}") String key,
                      @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
                      @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
                      @Value("${security.jwt.issuer}") String issuer) {

        if (Strings.isNullOrEmpty(key) || key.length() < 64) {
            System.out.println("key-"+key);
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }
        this.key = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    public String generateJwtToken(User user,String uuid){
        Instant now = Instant.now();

        return Jwts.builder()
                .id(uuid)
                .subject(user.getEmailId())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of("userId",user.getId(),
                        "emailId",user.getEmailId(),
                        "roles",user.getRoles(),
                        "orgId",user.getOrganizationId(),
                        "type","access"))
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String token) {
        Instant now = Instant.now();
        // Parse and validate the existing token
        Claims claims = this.parseToken(token);
        return Jwts.builder()
                .id(claims.getId())
                .subject(claims.get("emailId", String.class))
                .issuer(issuer).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claims(Map.of("userId", claims.get("userId"),
                        "emailId", claims.get("emailId"),
                        "roles", claims.get("roles"),
                        "orgId", claims.get("orgId"),
                        "type", "refresh"))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String refreshJwtToken(String token) {

        Instant now = Instant.now();

        // Parse and validate refresh token
        Claims claims = this.parseToken(token);

        // Make sure it is actually a refresh token
        String type = claims.get("type", String.class);

        if (!"refresh".equals(type)) {
            throw new ApiException(
                    "Invalid Refresh Token",
                    HttpStatus.FORBIDDEN,
                    new IllegalArgumentException("Invalid Refresh Token")
            );
        }

        // Get the refresh token's original expiry
        Date refreshExpiry = claims.getExpiration();

        Instant refreshExpiryInstant =
                refreshExpiry.toInstant();

        // Check whether refresh token has expired
        if (!now.isBefore(refreshExpiryInstant)) {

            throw new ApiException(
                    "Refresh Token Expired",
                    HttpStatus.FORBIDDEN,
                    new IllegalArgumentException("Refresh Token Expired")
            );
        }

        // Normally access token should live for 10 minutes
        Instant accessExpiry =
                now.plusSeconds(accessTtlSeconds);

        /*
         * IMPORTANT:
         *
         * Don't allow the new access token to live
         * beyond the refresh token's expiry.
         */
        if (accessExpiry.isAfter(refreshExpiryInstant)) {
            accessExpiry = refreshExpiryInstant;
        }

        // Generate NEW ACCESS TOKEN
        return Jwts.builder()
                .id(claims.getId())
                .subject(claims.get("emailId", String.class))
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiry))
                .claims(Map.of(
                        "userId", claims.get("userId"),
                        "emailId", claims.get("emailId"),
                        "roles", claims.get("roles"),
                        "orgId", claims.get("orgId"),
                        "type", "access"
                ))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmailId(String token){
        Claims claims = this.parseToken(token);
        return claims.get("subject",String.class);
    }

    public Long extractUserId(String token){
        Claims claims = this.parseToken(token);
        return claims.get("userId",Long.class);
    }

    public boolean isAccessToken(String token) {

        Claims claims = parseToken(token);

        return "access".equals(
                claims.get("type", String.class)
        );
    }

    public boolean isRefreshToken(String token) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Claims claims = parseToken(token);

        return "refresh".equals(
                claims.get("type", String.class)
        );
    }

    public String extractRoles(String token){
        Claims claims = this.parseToken(token);
        return claims.get("roles",String.class);
    }

    public Long extractOrgId(String token){
        Claims claims = this.parseToken(token);
        return claims.get("orgId",Long.class);
    }

    public Set<Permissions> extractPermissions(String token){
        Claims claims = this.parseToken(token);
        return claims.get("permissions", Set.class);
    }

    public Instant getRefreshExpiry(String token) {

        Claims claims = parseToken(token);

        return claims.getExpiration().toInstant();
    }

    private boolean accessExpiresAfter(
            Instant accessExpiry,
            Instant refreshExpiry
    ) {
        return accessExpiry.isAfter(refreshExpiry);
    }

}

package com.gen.auth.security;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {

    private final String cookieName;
    private final String cookieDomain;
    private final String cookiePath;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieSameSite;


    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String cookieName,
            @Value("${security.jwt.refresh-token-cookie-domain}") String cookieDomain,
            @Value("${security.jwt.refresh-token-cookie-path}") String cookiePath,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite) {

        this.cookieName = cookieName;
        this.cookieDomain = cookieDomain;
        this.cookiePath = cookiePath;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSameSite = cookieSameSite;
    }

    //create method to attach cookie to response
    public void attachCookie(HttpServletResponse response, String value,int maxAge) {

        var responseCookieBuilder =ResponseCookie.from(cookieName,value)
                .httpOnly(cookieHttpOnly)
                .path(cookiePath)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(maxAge);

        if(!Strings.isNullOrEmpty(cookieDomain)){
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    //method to remove cookie
    public void removeCookie(HttpServletResponse response) {
        attachCookie(response, "", 0);
    }

    public void addNoStore(HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");
    }

    public void clearCookie(HttpServletResponse response) {
        removeCookie(response);
        addNoStore(response);
    }
}

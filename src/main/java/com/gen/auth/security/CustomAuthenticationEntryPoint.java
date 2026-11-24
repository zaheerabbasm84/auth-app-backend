package com.gen.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gen.auth.apiexception.ErrorCodes;
import com.gen.auth.apiexception.GlobalExceptionHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);


    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {


        String message = authException.getMessage();
        String code = "401";
        Object errorCodeObject = request.getAttribute("errorCode");

        if (errorCodeObject instanceof ErrorCodes errorCode) {
            message = errorCode.getMessage();
            code = String.valueOf(errorCode.getCode());
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        Map<String, Object> error = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "code",code,
                "message", message
        );

        log.error("ERROR IN JWT :: {}", error, authException);


        response.getWriter()
                .write(objectMapper.writeValueAsString(error));
    }
}

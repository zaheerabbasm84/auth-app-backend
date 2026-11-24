package com.gen.auth.security.helperDTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private Long userId;
    private String token;
    private String refreshToken;
    private String tokenType;

    public LoginResponse of(Long userId, String token, String refreshToken){
        LoginResponse response = new LoginResponse();
        response.setUserId(userId);
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        return response;
    }

    public LoginResponse(Long userId, String token, String refreshToken){
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }

}

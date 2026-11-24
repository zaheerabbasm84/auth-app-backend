package com.gen.auth.service;

import com.gen.auth.dto.UserDto;
import com.gen.auth.security.helperDTOs.LoginRefreshRequest;
import com.gen.auth.security.helperDTOs.LoginRequest;
import com.gen.auth.security.helperDTOs.LoginResponse;
import com.gen.auth.security.helperDTOs.LogoutRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    public UserDto register(UserDto userDto);

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response);

    public LoginResponse refreshAccessToken(LoginRefreshRequest loginRefreshRequest, HttpServletResponse response, HttpServletRequest request);

    void logout(LogoutRequest logoutRequest, HttpServletResponse response, HttpServletRequest request);
}

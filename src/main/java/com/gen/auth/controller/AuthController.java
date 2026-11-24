package com.gen.auth.controller;

import com.gen.auth.apiresponse.ApiResponse;
import com.gen.auth.apiresponse.Payload;
import com.gen.auth.dto.UserDto;
import com.gen.auth.security.helperDTOs.LoginRefreshRequest;
import com.gen.auth.security.helperDTOs.LoginRequest;
import com.gen.auth.security.helperDTOs.LoginResponse;
import com.gen.auth.security.helperDTOs.LogoutRequest;
import com.gen.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse registerUser(@RequestBody UserDto userDto){
        userDto = authService.register(userDto);
        return ApiResponse.buildWithPayload(new Payload<UserDto>().addObject(userDto).addMessage("CREATED")).setStatus(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest loginRequest,HttpServletResponse response){
        LoginResponse login = authService.login(loginRequest,response);
        return ApiResponse.buildWithPayload(new Payload<LoginResponse>().addObject(login).addMessage("LOGGEDIN")).setStatus(HttpStatus.OK);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refresh(@RequestBody LoginRefreshRequest loginRefreshRequest, HttpServletResponse response, HttpServletRequest request) {
        return ResponseEntity.ok(
                authService.refreshAccessToken(loginRefreshRequest,response,request));
    }

    @PostMapping("/logout")
    public ApiResponse logout(@RequestBody LogoutRequest logoutRequest, HttpServletResponse response, HttpServletRequest request) {
        authService.logout(logoutRequest,response,request);
        return ApiResponse.buildWithPayload(new Payload<>().addMessage("LOGOUT")).setStatus(HttpStatus.OK);
    }
}

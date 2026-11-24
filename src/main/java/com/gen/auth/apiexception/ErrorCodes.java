package com.gen.auth.apiexception;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(
        shape = JsonFormat.Shape.OBJECT
)
@Getter
public enum ErrorCodes {

    USER_NOT_FOUND(1001,"User Is Not Present In The System"),
    USER_NAME_ALREADY_EXIST(1002,"User Name Already Exist"),
    EMAIL_ALREADY_EXIST(1003,"Email Id Already Exist"),
    EMAIL_IS_REQUIRED(1004,"Email Id Is Required"),
    USER_NAME_IS_REQUIRED(1005,"User name is required"),
    ORGANIZATION_IS_REQUIRED(1006,"Organization is required"),
    INVALID_EMAIL_ID_OR_PASSWORD(1007,"Invalid Email Id or Password"),
    USER_IS_INACTIVE(1008,"User is not active"),
    INVALID_AUTHORIZATION_TOKEN(1009,"Invalid authorization token"),
    EXPIRED_AUTHORIZATION_TOKEN(1010,"Authorization token has expired"),
    MALFORMED_AUTHORIZATION_TOKEN(1011,"Authorization token is malformed"),
    JWT_AUTHENTICATION_FAILED(1012,"JWT authentication failed"),
    USER_IS_UNAUTHORIZED(1013,"User is Unauthorized"),
    INVALID_REFRESH_TOKEN(1014,"Invalid refresh token"),
    REFRESH_TOKEN_REQUIRED(1015,"Refresh token is required"),
    REFRESH_TOKEN_NOT_FOUND(1016,"Refresh token is not found"),
    REFRESH_TOKEN_REVOKED(1016,"Refresh token is expired or revoked. Please login again.");

    int code;
    String message;

    private ErrorCodes(int code,String message){
        this.code=code;
        this.message=message;
    }
}

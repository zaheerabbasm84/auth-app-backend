package com.gen.auth.apiresponse;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Getter
public class ApiResponse {
    private Payload payload;
    private transient HttpStatus status;

    private ApiResponse(Payload payload) {
        this.payload = payload;
    }


    public static ApiResponse build() {
        return new ApiResponse();
    }

    public static ApiResponse buildWithPayload(Payload payload) {
        return new ApiResponse(payload);
    }

    public ApiResponse setStatus(HttpStatus status){
        this.status= status;
        return this;
    }
}

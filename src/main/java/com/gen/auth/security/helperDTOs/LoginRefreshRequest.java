package com.gen.auth.security.helperDTOs;

import lombok.Getter;


public record LoginRefreshRequest(
        String refreshToken
) {
}

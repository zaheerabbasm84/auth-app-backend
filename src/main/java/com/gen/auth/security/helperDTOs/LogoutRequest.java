package com.gen.auth.security.helperDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(


/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
        /*
        Records are like classes but with additional features.
        Records are final, they can't be extended.
        Records can't be abstract.
        Records are immutable by default.
        Records are serializable by default.
        Records generate equals, hashCode, toString, compareTo based on their fields.
        Records don't need to provide constructors.
        Records can't have private fields.
        Records can have private constructors.
        Records can have static methods.
        Records can have instance methods.
        Records can have private methods.
        Records can have private static methods.
        Records can have static fields.
        Records can have instance fields.
        */
/* <<<<<<<<<<  6383c595-1fdd-4925-a8b1-8689985a535c  >>>>>>>>>>> */

        @NotBlank(message = "UserId is required")
        String userId,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        Long organizationId,
        String refreshToken
) {
}

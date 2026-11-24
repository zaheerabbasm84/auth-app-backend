package com.gen.auth.dto;

import com.gen.auth.helperutils.ModelDTO;
import com.gen.auth.model.Provider;
import com.gen.auth.model.Roles;
import com.gen.auth.model.Status;
import com.gen.auth.model.UserRoles;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends ModelDTO {

    private String firstName;
    private String lastName;
    private String userName;
    private String emailId;
    private String password;
    private String verificationToken;
    private String adUserId;
    private Boolean isEnabled;
    private String userType;
    private Boolean isAdmin=Boolean.FALSE;
    private Long organizationId;
    private Set<String> roles = new HashSet<>();
    private List<UserRoles> userRoles = new ArrayList<>();
    private Provider provider = Provider.LOCAL;
    private Status status = Status.pending;
    private String gender;
}

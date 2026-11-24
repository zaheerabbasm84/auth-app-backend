package com.gen.auth.model;

import com.gen.auth.helperutils.Model;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserRoles extends Model {
    private Long userId;
    private Long roleId;
    private Boolean currentRole = Boolean.FALSE;
}

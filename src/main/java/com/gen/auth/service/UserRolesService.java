package com.gen.auth.service;

import com.gen.auth.model.UserRoles;

import java.util.List;

public interface UserRolesService {

    public List<UserRoles> bulkCreate(List<UserRoles> userRolesList);
}


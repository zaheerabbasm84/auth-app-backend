package com.gen.auth.service.Impl;

import com.gen.auth.helperutils.ObjectUtils;
import com.gen.auth.model.UserRoles;
import com.gen.auth.repository.UserRolesRepository;
import com.gen.auth.service.UserRolesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRolesServiceImpl implements UserRolesService {
    private final UserRolesRepository userRolesRepository;
    @Override
    public List<UserRoles> bulkCreate(List<UserRoles> userRolesList) {
        if(!ObjectUtils.isNullOrEmpty(userRolesList)){
            return userRolesRepository.saveAll(userRolesList);
        }
        return List.of();
    }
}

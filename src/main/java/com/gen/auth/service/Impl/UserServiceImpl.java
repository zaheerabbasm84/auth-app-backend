package com.gen.auth.service.Impl;

import com.gen.auth.apiexception.ApiException;
import com.gen.auth.apiexception.ErrorCodes;
import com.gen.auth.dto.UserDto;
import com.gen.auth.dto.UserRolesDto;
import com.gen.auth.helperutils.ObjectUtils;
import com.gen.auth.model.Permissions;
import com.gen.auth.model.Roles;
import com.gen.auth.model.User;
import com.gen.auth.model.UserRoles;
import com.gen.auth.repository.UserRepository;
import com.gen.auth.repository.UserRolesRepository;
import com.gen.auth.repositoryImpl.UserRepositoryImp;
import com.gen.auth.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private  final UserRepositoryImp userRepositoryImp;
    private final UserRolesServiceImpl userRolesService;
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmailId() == null || userDto.getEmailId().isBlank()){
            throw new ApiException("Email id is required", HttpStatus.BAD_REQUEST,new IllegalStateException("Email id is required")).addErrors(ErrorCodes.EMAIL_IS_REQUIRED);
        }
        if (userDto.getUserName() == null || userDto.getUserName().isBlank()) {
            throw new ApiException(
                    "User name is required",
                    HttpStatus.BAD_REQUEST,
                    new IllegalStateException("User name is required"))
                    .addErrors(ErrorCodes.USER_NAME_IS_REQUIRED);
        }

        if (userDto.getOrganizationId() == null) {
            throw new ApiException(
                    "Organization is required",
                    HttpStatus.BAD_REQUEST,
                    new IllegalStateException("Organization is required"))
                    .addErrors(ErrorCodes.ORGANIZATION_IS_REQUIRED);
        }
        if(userRepository.existsByUserNameAndOrganizationId(userDto.getUserName(),userDto.getOrganizationId())){
            throw  new ApiException("User Name Already Exist", HttpStatus.BAD_REQUEST,new IllegalStateException("User Name Already Exist")).addErrors(ErrorCodes.USER_NAME_ALREADY_EXIST);
        }
        userDto.setUserName(userDto.getEmailId());

        User user = modelMapper.map(userDto,User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        for(UserRoles userRoles : userDto.getUserRoles()){
            userRoles.setUserId(user.getId());
        }

        List<UserRoles> userRolesList = userRolesService.bulkCreate(userDto.getUserRoles());

        user.setUserRoles(userRolesList);

        user.setRoles(userRepositoryImp.getRolesByUserId(user.getId()));

        System.out.println("Before mapping: " + user.getUserName());

        UserDto dto = modelMapper.map(user, UserDto.class);

        System.out.println("After mapping: " + dto.getUserName());

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("Resource Not Found", HttpStatus.NOT_FOUND)
                .addErrors(ErrorCodes.USER_NOT_FOUND));
        if (userRepository.existsByUserNameAndOrganizationIdAndIdNot(userDto.getUserName(),userDto.getOrganizationId(),userId)) {
            throw new ApiException("User Name Already Exist",HttpStatus.BAD_REQUEST,new IllegalStateException("User Name Already Exist")).addErrors(ErrorCodes.USER_NAME_ALREADY_EXIST);
        }
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user=user.merge(userDto);
        entityManager.flush();
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String emailId) {
        User user = userRepository.findByEmailId(emailId).orElseThrow(() -> new ApiException("Resource Not Found", HttpStatus.NOT_FOUND)
                .addErrors(ErrorCodes.USER_NOT_FOUND));
        Long userId = user.getId();
        Set<String> roles = userRepositoryImp.getRolesByUserId(userId);
        List<Permissions> permissionsSet = userRepositoryImp.getPermissions(userId);
        user.setRoles(roles);
        user.setPermissions(Set.copyOf(permissionsSet));
        return user;
    }

    @Override
    @Transactional
    public UserDto deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("Resource Not Found", HttpStatus.NOT_FOUND)
                .addErrors(ErrorCodes.USER_NOT_FOUND));
        userRepository.deleteById(userId);
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long userId) {
        return  userRepository.findById(userId).map(user -> modelMapper.map(user,UserDto.class)).orElseThrow(() -> new ApiException("Resource Not Found", HttpStatus.NOT_FOUND)
                .addErrors(ErrorCodes.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> modelMapper.map(user,UserDto.class)).toList();
    }


}

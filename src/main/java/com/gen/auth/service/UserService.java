package com.gen.auth.service;

import com.gen.auth.dto.UserDto;
import com.gen.auth.model.User;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto,Long userId);

    User getUserByEmail(String emailId);

    UserDto deleteUser(Long userId);

    UserDto findById(Long userId);

    List<UserDto> getAllUsers();
}

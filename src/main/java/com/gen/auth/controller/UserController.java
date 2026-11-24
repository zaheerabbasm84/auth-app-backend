package com.gen.auth.controller;

import com.gen.auth.dto.UserDto;
import com.gen.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto,@PathVariable("id") Long id){
        UserDto userDto1 = userService.updateUser(userDto,id);
        return ResponseEntity.status(HttpStatus.OK).body(userDto1);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id){
        UserDto userDto1 = userService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userDto1);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable("id") Long id){
        UserDto userDto = userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }



}

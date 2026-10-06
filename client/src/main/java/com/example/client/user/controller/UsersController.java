package com.example.client.user.controller;

import com.example.client.user.dto.CreateUserDto;
import com.example.client.user.dto.UserDto;
import com.example.client.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UsersController {
    private final UserService userService;

    /** Admin: every user. */
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.findAllUsers(), HttpStatus.OK);
    }

    /** Admin: create a user with any role. */
    @PostMapping("/add")
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody CreateUserDto createUserDto){
        return new ResponseEntity<>(userService.createUser(createUserDto), HttpStatus.CREATED);
    }

    /** Staff see customers and customers see staff: the people they can raise a clarification to. */
    @GetMapping("/assignable")
    public ResponseEntity<List<UserDto>> getAssignableUsers(Authentication authentication) {
        return new ResponseEntity<>(userService.findAssignableUsers(userService.currentUser(authentication)), HttpStatus.OK);
    }
}

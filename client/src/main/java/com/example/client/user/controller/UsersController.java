package com.example.client.user.controller;

import com.example.client.requestclarification.model.Clarification;
import com.example.client.user.model.User;
import com.example.client.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UsersController {
    private final UserService userService;

    @GetMapping("/findByEmail/{email}")
    public ResponseEntity<User> findCustomerByEmail(@PathVariable String email){
        User userEmail = userService.findByEmail(email);
        return new ResponseEntity<>(userEmail, HttpStatus.OK);
    }

    @GetMapping("/findByUserName/{userName}")
    public ResponseEntity<User> findCustomerByUserName(@PathVariable String userName){
        User userName1 = userService.findByUserName(userName);
        return new ResponseEntity<>(userName1, HttpStatus.OK);
    }


//    @PostMapping("/add")
//    public ResponseEntity<User> addUser(@RequestBody User user){
//        User newUser = userService.addUser(user);
//        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
//    }

    @RequestMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);


    }
}

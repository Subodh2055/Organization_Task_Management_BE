package com.example.client.user.controller;


import com.example.client.user.dto.AuthResponse;
import com.example.client.user.dto.LoginDto;
import com.example.client.user.dto.SignUpDto;
import com.example.client.user.dto.UserDto;
import com.example.client.user.model.User;
import com.example.client.user.repository.UserRepository;
import com.example.client.user.service.JwtService;
import com.example.client.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginDto loginDto){
        // Throws BadCredentialsException (401) when the username or password is wrong.
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(),
                loginDto.getPassword()));
        User user = userService.currentUser(authentication);
        return new ResponseEntity<>(jwtService.issueToken(user), HttpStatus.OK);
    }

    /** Public self-registration: always creates a CUSTOMER. */
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpDto signUpDto){
        if (!signUpDto.getPassword().equals(signUpDto.getPasswordConfirm())) {
            return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
        userService.registerCustomer(signUpDto);
        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> currentUser(Authentication authentication) {
        return new ResponseEntity<>(UserDto.from(userService.currentUser(authentication)), HttpStatus.OK);
    }

    /** Lets the sign-up form flag a taken username or email without exposing any user data. */
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@RequestParam(required = false) String userName,
                                                                  @RequestParam(required = false) String email) {
        boolean userNameTaken = userName != null && !userName.isBlank() && userRepository.existsByUserName(userName);
        boolean emailTaken = email != null && !email.isBlank() && userRepository.existsByEmail(email);
        return new ResponseEntity<>(Map.of("userNameTaken", userNameTaken, "emailTaken", emailTaken), HttpStatus.OK);
    }

}

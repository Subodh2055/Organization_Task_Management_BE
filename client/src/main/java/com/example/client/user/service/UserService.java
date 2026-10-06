package com.example.client.user.service;

import com.example.client.user.dto.CreateUserDto;
import com.example.client.user.dto.SignUpDto;
import com.example.client.user.dto.UserDto;
import com.example.client.user.model.Role;
import com.example.client.user.model.RoleName;
import com.example.client.user.model.User;
import com.example.client.user.repository.RoleRepository;
import com.example.client.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }

    /** The logged-in user, resolved from the JWT subject. */
    public User currentUser(Authentication authentication) {
        return userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
    }

    public boolean hasRole(User user, String roleName) {
        return user.getRoles() != null && user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }

    /** Staff raise clarifications to customers and customers raise them to staff. */
    public String counterpartRole(User user) {
        if (hasRole(user, RoleName.STAFF)) {
            return RoleName.CUSTOMER;
        }
        if (hasRole(user, RoleName.CUSTOMER)) {
            return RoleName.STAFF;
        }
        return null;
    }

    public List<UserDto> findAssignableUsers(User user) {
        String counterpart = counterpartRole(user);
        if (counterpart == null) {
            return List.of();
        }
        return userRepository.findByRoles_Name(counterpart).stream().map(UserDto::from).toList();
    }

    public UserDto createUser(CreateUserDto dto) {
        String roleName = RoleName.normalize(dto.getRole());
        if (roleName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be ADMIN, STAFF or CUSTOMER");
        }
        ensureAvailable(dto.getUserName(), dto.getEmail());
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setDesignation(dto.getDesignation());
        user.setOrganizationName(dto.getOrganizationName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setUserName(dto.getUserName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(roles(roleName));
        return UserDto.from(userRepository.save(user));
    }

    /** Public self-registration: always a CUSTOMER. */
    public void registerCustomer(SignUpDto dto) {
        ensureAvailable(dto.getUserName(), dto.getEmail());
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setUserName(dto.getUserName());
        user.setDesignation(dto.getDesignation());
        user.setOrganizationName(dto.getOrganizationName());
        user.setMobile(dto.getMobile());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(roles(RoleName.CUSTOMER));
        userRepository.save(user);
    }

    public void ensureAvailable(String userName, String email) {
        if (userRepository.existsByUserName(userName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken!");
        }
    }

    public Set<Role> roles(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
        return new HashSet<>(Set.of(role));
    }
}

package com.example.client.config;

import com.example.client.user.model.Role;
import com.example.client.user.model.RoleName;
import com.example.client.user.model.User;
import com.example.client.user.repository.RoleRepository;
import com.example.client.user.repository.UserRepository;
import com.example.client.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Creates the three roles and, if none exists yet, the first ADMIN account. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;
    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;
    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String roleName : List.of(RoleName.ADMIN, RoleName.STAFF, RoleName.CUSTOMER)) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
            }
        }

        if (userRepository.existsByRoles_Name(RoleName.ADMIN)) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("No ADMIN account exists. Set ADMIN_PASSWORD (and optionally ADMIN_USERNAME, ADMIN_EMAIL) and restart to create one.");
            return;
        }
        if (userRepository.existsByUserName(adminUsername) || userRepository.existsByEmail(adminEmail)) {
            log.warn("Cannot create the ADMIN account: username {} or email {} is already used.", adminUsername, adminEmail);
            return;
        }
        User admin = new User();
        admin.setFullName("Administrator");
        admin.setDesignation("Administrator");
        admin.setEmail(adminEmail);
        admin.setMobile("0000000000");
        admin.setUserName(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(userService.roles(RoleName.ADMIN));
        userRepository.save(admin);
        log.info("Created ADMIN account {}.", adminUsername);
    }
}

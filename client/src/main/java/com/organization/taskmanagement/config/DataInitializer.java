package com.organization.taskmanagement.config;

import com.organization.taskmanagement.user.entity.Role;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.repository.RoleRepository;
import com.organization.taskmanagement.user.repository.UserRepository;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the three roles and, if none exists yet, the first ADMIN account. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName.authority()).isEmpty()) {
                roleRepository.save(new Role(roleName.authority()));
            }
        }

        if (userRepository.existsByRoles_Name(RoleName.ADMIN.authority())) {
            return;
        }
        AppProperties.Admin admin = properties.admin();
        if (admin.password() == null || admin.password().isBlank()) {
            log.warn("No ADMIN account exists. Set ADMIN_PASSWORD (and optionally ADMIN_USERNAME, ADMIN_EMAIL) and restart to create one.");
            return;
        }
        if (userRepository.existsByUserName(admin.username()) || userRepository.existsByEmailIgnoreCase(admin.email())) {
            log.warn("Cannot create the ADMIN account: username {} or email {} is already used.", admin.username(), admin.email());
            return;
        }
        User user = new User();
        user.setFullName("Administrator");
        user.setDesignation("Administrator");
        user.setEmail(admin.email());
        user.setMobile("0000000000");
        user.setUserName(admin.username());
        user.setPassword(passwordEncoder.encode(admin.password()));
        user.setRoles(userService.roles(RoleName.ADMIN));
        userRepository.save(user);
        log.info("Created ADMIN account {}.", admin.username());
    }
}

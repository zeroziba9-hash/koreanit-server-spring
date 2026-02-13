package com.koreanit.spring.security;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.koreanit.spring.user.UserEntity;
import com.koreanit.spring.user.UserRepository;
import com.koreanit.spring.user.UserService;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(
            UserRepository userRepository,
            UserService userService,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Long adminId;

        try {
            UserEntity admin = userRepository.findByUsername("admin");
            adminId = admin.getId();

            // 요구사항 고정값 보장
            userRepository.updateNickname(adminId, "관리자");
            userRepository.updatePassword(adminId, passwordEncoder.encode("1234"));
        } catch (EmptyResultDataAccessException e) {
            adminId = userService.create("admin", "1234", null, "관리자");
        }

        List<String> roles = userRoleRepository.findRolesByUserId(adminId);
        if (!roles.contains("ROLE_ADMIN")) {
            userRoleRepository.addRole(adminId, "ROLE_ADMIN");
        }
        if (!roles.contains("ROLE_USER")) {
            userRoleRepository.addRole(adminId, "ROLE_USER");
        }
    }
}

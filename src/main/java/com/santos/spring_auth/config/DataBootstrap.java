package com.santos.spring_auth.config;

import com.santos.spring_auth.entity.UserEntity;
import com.santos.spring_auth.enumeration.Role;
import com.santos.spring_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    @Override
    public void run(String... args) {
        this.seed(this.properties.adminUsername(), this.properties.adminEmail(), this.properties.adminPassword(), Role.ADMIN);
    }

    private void seed(String username, String email, String rawPassword, Role role) {
        if (username == null || rawPassword == null) {
            log.warn("Skipping bootstrap for role {} (missing username/password)", role);
            return;
        }
        if (this.userRepository.existsByUsername(username)) {
            return;
        }
        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .password(this.passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        this.userRepository.save(user);
        log.info("Bootstrapped {} user '{}'", role, username);
    }
}

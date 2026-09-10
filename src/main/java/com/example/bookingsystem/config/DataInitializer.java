package com.example.bookingsystem.config;

import com.example.bookingsystem.entity.Role;
import com.example.bookingsystem.entity.User;
import com.example.bookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin", "admin123", Role.ADMIN);
        seedUser("user", "user123", Role.USER);
    }

    private void seedUser(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        userRepository.save(user);
        log.info("Seeded {} user '{}'", role, username);
    }
}

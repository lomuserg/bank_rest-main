package com.example.bankcards.config.auth;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {
            String adminLogin = "admin";

            if (userRepository.findByLogin(adminLogin).isEmpty()) {
                User admin = User.builder()
                        .login(adminLogin)
                        .password(passwordEncoder.encode("admin"))
                        .phone("+70000000000")
                        .role(UserRole.ADMIN)
                        .banned(false)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}

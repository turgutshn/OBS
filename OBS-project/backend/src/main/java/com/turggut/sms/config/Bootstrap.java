package com.turggut.sms.config;

import com.turggut.sms.domain.user.Role;
import com.turggut.sms.domain.user.User;
import com.turggut.sms.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    public void run(String... args) {
        var cfg = appProperties.bootstrap();
        if (userRepository.findByUsername(cfg.adminUsername()).isPresent()) {
            return;
        }
        User admin = User.builder()
                .username(cfg.adminUsername())
                .email(cfg.adminEmail())
                .passwordHash(passwordEncoder.encode(cfg.adminPassword()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.warn("Bootstrap admin '{}' created. CHANGE THE PASSWORD AFTER FIRST LOGIN.",
                cfg.adminUsername());
    }
}

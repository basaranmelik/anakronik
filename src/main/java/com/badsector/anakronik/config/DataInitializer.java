package com.badsector.anakronik.config;

import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.UserRole;
import com.badsector.anakronik.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNeeded();
    }

    private void createAdminUserIfNeeded() {
        String adminEmail = "admin@anakronik.com";
        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            User admin = new User();
            admin.setFullName("Admin User");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created: " + adminEmail);
        }
    }
}
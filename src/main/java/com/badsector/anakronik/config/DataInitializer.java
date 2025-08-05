package com.badsector.anakronik.config;

import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.UserRole;
import com.badsector.anakronik.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.data-initializer.create-admin-user", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.data-initializer.admin-fullname}")
    private String adminFullName;

    @Value("${app.data-initializer.admin-email}")
    private String adminEmail;

    @Value("${app.data-initializer.admin-password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNeeded();
    }

    private void createAdminUserIfNeeded() {
        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            User admin = new User();
            admin.setFullName(adminFullName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            logger.info("Default admin user created successfully: {}", adminEmail);
        } else {
            logger.warn("Default admin user '{}' already exists. Skipping creation.", adminEmail);
        }
    }
}
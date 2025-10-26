package com.waitlist.infrastructure.config;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.UserRole;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin@waitlist.com").isEmpty()) {
            // Create default business
            Business business = new Business(
                    "Default Restaurant",
                    BusinessType.RESTAURANT,
                    "123 Main St, City, State",
                    "+1234567890",
                    "admin@restaurant.com",
                    50,
                    60);
            Business savedBusiness = businessRepository.save(business);

            // Create admin user
            User adminUser = new User(
                    "admin@waitlist.com",
                    passwordEncoder.encode("admin123"),
                    "admin@waitlist.com",
                    savedBusiness,
                    UserRole.BUSINESS_OWNER);
            userRepository.save(adminUser);

            System.out.println("Default admin user created: admin@waitlist.com / admin123");
        }
    }
}


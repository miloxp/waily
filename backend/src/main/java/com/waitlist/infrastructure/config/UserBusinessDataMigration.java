package com.waitlist.infrastructure.config;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.User;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Migration component to assign businesses to existing users that don't have any businesses.
 * This ensures that users created before the many-to-many migration have businesses assigned.
 */
@Component
@Order(5) // Run after DataInitializer (Order 4) to assign businesses to newly created users
public class UserBusinessDataMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserBusinessDataMigration.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            logger.info("Running user-business data migration: Assigning businesses to users without businesses");

            // Get all users with their businesses loaded
            List<User> allUsers = userRepository.findAllWithBusinesses();
            int migrated = 0;

            for (User user : allUsers) {
                // Check if user has any businesses
                if (user.getBusinesses() == null || user.getBusinesses().isEmpty()) {
                    logger.info("User {} has no businesses, attempting to assign one", user.getUsername());

                    // Try to find a business for this user based on email or create a default one
                    Business business = findOrCreateBusinessForUser(user);
                    
                    if (business != null) {
                        user.addBusiness(business);
                        userRepository.save(user);
                        migrated++;
                        logger.info("Assigned business '{}' (ID: {}) to user {}", 
                                business.getName(), business.getId(), user.getUsername());
                    } else {
                        logger.warn("Could not find or create business for user {}", user.getUsername());
                    }
                } else {
                    logger.debug("User {} already has {} businesses", user.getUsername(), user.getBusinesses().size());
                }
            }

            logger.info("User-business data migration completed. Migrated {} users.", migrated);
        } catch (Exception e) {
            logger.error("Error during user-business data migration: {}", e.getMessage(), e);
            // Don't throw - allow application to continue
        }
    }

    private Business findOrCreateBusinessForUser(User user) {
        String username = user.getUsername();
        String email = user.getEmail();
        
        // For PLATFORM_ADMIN, find Platform Business
        if (user.getRole().name().equals("PLATFORM_ADMIN")) {
            return businessRepository.findAll().stream()
                    .filter(b -> b.getName().equals("Platform Business"))
                    .findFirst()
                    .orElse(null);
        }

        // Map users to their businesses based on username/email patterns
        List<Business> allBusinesses = businessRepository.findAll();
        
        // demo-owner@restaurant.com, demo-staff@restaurant.com -> Demo Restaurant
        if (username.contains("demo") && (username.contains("restaurant") || email.contains("restaurant"))) {
            return allBusinesses.stream()
                    .filter(b -> b.getName().equals("Demo Restaurant"))
                    .findFirst()
                    .orElse(null);
        }
        
        // demo2-owner@cafe.com -> Demo Café
        if (username.contains("demo2") || (username.contains("demo") && email.contains("cafe"))) {
            return allBusinesses.stream()
                    .filter(b -> b.getName().equals("Demo Café"))
                    .findFirst()
                    .orElse(null);
        }
        
        // admin@waitlist.com -> Default Restaurant
        if (username.equals("admin@waitlist.com")) {
            return allBusinesses.stream()
                    .filter(b -> b.getName().equals("Default Restaurant"))
                    .findFirst()
                    .orElse(null);
        }
        
        // testowner -> try to find by email domain
        String emailDomain = email.contains("@") ? email.split("@")[1] : "";
        return allBusinesses.stream()
                .filter(b -> b.getEmail() != null && b.getEmail().contains(emailDomain))
                .findFirst()
                .orElse(null);
    }
}


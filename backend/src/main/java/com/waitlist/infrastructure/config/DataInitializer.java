package com.waitlist.infrastructure.config;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.UserRole;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(4) // Run after all migrations (DatabaseMigration=1, RemoveBusinessManagerMigration=2, SubscriptionMigration=3)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create PLATFORM_ADMIN user (Platform Owner)
        if (userRepository.findByUsername("platform@waitlist.com").isEmpty()) {
            // Create a special "Platform" business for platform admin
            Business platformBusiness = new Business(
                    "Platform Business",
                    BusinessType.OTHER,
                    "Platform Headquarters",
                    "+10000000000",
                    "platform@waitlist.com",
                    0,
                    0);
            platformBusiness.setIsActive(true);
            Business savedPlatformBusiness = businessRepository.save(platformBusiness);

            User platformAdmin = new User(
                    "platform@waitlist.com",
                    passwordEncoder.encode("platform123"),
                    "platform@waitlist.com",
                    UserRole.PLATFORM_ADMIN);
            platformAdmin.addBusiness(savedPlatformBusiness);
            userRepository.save(platformAdmin);

            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("PLATFORM ADMIN CREATED:");
            System.out.println("Username: platform@waitlist.com");
            System.out.println("Password: platform123");
            System.out.println("Role: PLATFORM_ADMIN");
            System.out.println("═══════════════════════════════════════════════════════");
        }

        // Create demo business and users for demonstration
        if (userRepository.findByUsername("demo-owner@restaurant.com").isEmpty()) {
            // Create demo business
            Business demoBusiness = new Business(
                    "Demo Restaurant",
                    BusinessType.RESTAURANT,
                    "456 Demo Street, Demo City",
                    "+1555123456",
                    "demo@restaurant.com",
                    75,
                    45);
            Business savedDemoBusiness = businessRepository.save(demoBusiness);

            // Create BUSINESS_OWNER
            User businessOwner = new User(
                    "demo-owner@restaurant.com",
                    passwordEncoder.encode("owner123"),
                    "demo-owner@restaurant.com",
                    UserRole.BUSINESS_OWNER);
            businessOwner.addBusiness(savedDemoBusiness);
            userRepository.save(businessOwner);

            // Create BUSINESS_STAFF
            User businessStaff = new User(
                    "demo-staff@restaurant.com",
                    passwordEncoder.encode("staff123"),
                    "demo-staff@restaurant.com",
                    UserRole.BUSINESS_STAFF);
            businessStaff.addBusiness(savedDemoBusiness);
            userRepository.save(businessStaff);

            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("DEMO USERS CREATED (Demo Restaurant):");
            System.out.println("BUSINESS_OWNER:");
            System.out.println("  Username: demo-owner@restaurant.com");
            System.out.println("  Password: owner123");
            System.out.println("BUSINESS_STAFF:");
            System.out.println("  Username: demo-staff@restaurant.com");
            System.out.println("  Password: staff123");
            System.out.println("═══════════════════════════════════════════════════════");
        }

        // Create another demo business for multi-business demonstration
        if (userRepository.findByUsername("demo2-owner@cafe.com").isEmpty()) {
            Business demoBusiness2 = new Business(
                    "Demo Café",
                    BusinessType.CAFE,
                    "789 Coffee Lane, Demo City",
                    "+1555987654",
                    "demo2@cafe.com",
                    40,
                    30);
            Business savedDemoBusiness2 = businessRepository.save(demoBusiness2);

            User businessOwner2 = new User(
                    "demo2-owner@cafe.com",
                    passwordEncoder.encode("owner123"),
                    "demo2-owner@cafe.com",
                    UserRole.BUSINESS_OWNER);
            businessOwner2.addBusiness(savedDemoBusiness2);
            userRepository.save(businessOwner2);

            System.out.println("Additional demo business created: Demo Café");
            System.out.println("  Owner: demo2-owner@cafe.com / owner123");
        }

        // Keep the old admin user for backward compatibility
        if (userRepository.findByUsername("admin@waitlist.com").isEmpty()) {
            Business business = new Business(
                    "Default Restaurant",
                    BusinessType.RESTAURANT,
                    "123 Main St, City, State",
                    "+1234567890",
                    "admin@restaurant.com",
                    50,
                    60);
            Business savedBusiness = businessRepository.save(business);

            User adminUser = new User(
                    "admin@waitlist.com",
                    passwordEncoder.encode("admin123"),
                    "admin@waitlist.com",
                    UserRole.BUSINESS_OWNER);
            adminUser.addBusiness(savedBusiness);
            userRepository.save(adminUser);

            System.out.println("Legacy admin user created: admin@waitlist.com / admin123");
        }
    }
}


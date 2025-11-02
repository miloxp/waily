package com.waitlist.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Migration to remove BUSINESS_MANAGER role and all users with that role.
 * Also updates the database constraint to remove BUSINESS_MANAGER from allowed roles.
 */
@Component
@Order(2) // Run after DatabaseMigration (Order 1) but before DataInitializer (Order 2)
public class RemoveBusinessManagerMigration {

    private static final Logger logger = LoggerFactory.getLogger(RemoveBusinessManagerMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            logger.info("Running migration: Removing BUSINESS_MANAGER role and users");

            // Check if users table exists first
            try {
                jdbcTemplate.queryForObject("SELECT 1 FROM users LIMIT 1", Integer.class);
            } catch (Exception e) {
                logger.warn("Users table does not exist yet, skipping migration");
                return;
            }

            // Check if BUSINESS_MANAGER exists in the enum or users
            Integer managerUsersCount = null;
            try {
                managerUsersCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE role = 'BUSINESS_MANAGER'",
                    Integer.class
                );
            } catch (Exception e) {
                logger.warn("Could not query users table: {}", e.getMessage());
                return;
            }

            if (managerUsersCount != null && managerUsersCount > 0) {
                logger.info("Found {} users with BUSINESS_MANAGER role, deleting them...", managerUsersCount);
                
                // Delete users with BUSINESS_MANAGER role
                // First remove from user_businesses join table (CASCADE should handle this, but being explicit)
                jdbcTemplate.execute(
                    "DELETE FROM user_businesses WHERE user_id IN " +
                    "(SELECT id FROM users WHERE role = 'BUSINESS_MANAGER')"
                );
                
                // Delete the users
                int deleted = jdbcTemplate.update(
                    "DELETE FROM users WHERE role = 'BUSINESS_MANAGER'"
                );
                
                logger.info("Deleted {} users with BUSINESS_MANAGER role", deleted);
            } else {
                logger.info("No users with BUSINESS_MANAGER role found");
            }

            // Update the check constraint to remove BUSINESS_MANAGER
            try {
                logger.info("Updating users_role_check constraint to remove BUSINESS_MANAGER");
                
                // Drop the existing constraint
                jdbcTemplate.execute(
                    "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check"
                );
                
                // Recreate the constraint without BUSINESS_MANAGER
                jdbcTemplate.execute(
                    "ALTER TABLE users ADD CONSTRAINT users_role_check " +
                    "CHECK (role::text = ANY (ARRAY[" +
                    "'PLATFORM_ADMIN'::character varying, " +
                    "'BUSINESS_OWNER'::character varying, " +
                    "'BUSINESS_STAFF'::character varying" +
                    "]::text[]))"
                );
                
                logger.info("Successfully updated users_role_check constraint");
            } catch (Exception e) {
                logger.warn("Could not update users_role_check constraint: {}", e.getMessage());
                // Continue - the constraint update might fail if already done
            }

            logger.info("Migration completed: BUSINESS_MANAGER role removed");
        } catch (Exception e) {
            logger.error("Error during BUSINESS_MANAGER removal migration: {}", e.getMessage(), e);
            // Don't throw - allow application to continue
        }
    }
}


package com.waitlist.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Order(0) // Run before SubscriptionMigration
public class UserBusinessMigration {

    private static final Logger logger = LoggerFactory.getLogger(UserBusinessMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            logger.info("Running database migration: Converting users to support multiple businesses");

            // Check if user_businesses table exists
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name = 'user_businesses'",
                Integer.class
            );

            if (tableExists == null || tableExists == 0) {
                logger.info("Creating user_businesses join table");

                // Create the join table
                jdbcTemplate.execute(
                    "CREATE TABLE user_businesses (" +
                    "user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                    "business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE, " +
                    "PRIMARY KEY (user_id, business_id)" +
                    ")"
                );

                // Create index
                jdbcTemplate.execute(
                    "CREATE INDEX idx_user_businesses_user ON user_businesses(user_id)"
                );
                jdbcTemplate.execute(
                    "CREATE INDEX idx_user_businesses_business ON user_businesses(business_id)"
                );

                // Migrate existing data from business_id column to join table
                logger.info("Migrating existing user-business relationships");
                jdbcTemplate.execute(
                    "INSERT INTO user_businesses (user_id, business_id) " +
                    "SELECT id, business_id FROM users WHERE business_id IS NOT NULL"
                );

                // Note: We're not dropping the business_id column yet to maintain backward compatibility
                // The column can be removed in a future migration if needed
                logger.info("Migration completed successfully. Old business_id column kept for compatibility.");
            } else {
                logger.info("user_businesses table already exists");
            }

        } catch (Exception e) {
            logger.error("Error during user-business migration: {}", e.getMessage(), e);
            logger.error("Migration failed. Please run the SQL migration manually.");
            // Don't throw - allow application to continue if migration fails
        }
    }
}


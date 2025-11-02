package com.waitlist.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run before DataInitializer
public class DatabaseMigration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            logger.info("Running database migration: Adding PLATFORM_ADMIN role support");
            
            // Check if the constraint exists
            Integer constraintExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints " +
                "WHERE table_name = 'users' AND constraint_name = 'users_role_check'",
                Integer.class
            );

            if (constraintExists != null && constraintExists > 0) {
                // Constraint exists, check if it needs updating
                try {
                    // Try to insert a test value to see if PLATFORM_ADMIN is allowed
                    // This is safer than parsing the constraint definition
                    String checkClause = jdbcTemplate.queryForObject(
                        "SELECT check_clause FROM information_schema.check_constraints " +
                        "WHERE constraint_name = 'users_role_check'",
                        String.class
                    );
                    
                    if (checkClause != null && (!checkClause.contains("PLATFORM_ADMIN") || checkClause.contains("BUSINESS_MANAGER"))) {
                        logger.info("Updating users_role_check constraint to include PLATFORM_ADMIN");
                        
                        // Drop the old constraint
                        jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
                        
                        // Add the new constraint with PLATFORM_ADMIN (without BUSINESS_MANAGER)
                        jdbcTemplate.execute(
                            "ALTER TABLE users ADD CONSTRAINT users_role_check " +
                            "CHECK (((role)::text = ANY (ARRAY[" +
                            "('PLATFORM_ADMIN'::character varying)::text, " +
                            "('BUSINESS_OWNER'::character varying)::text, " +
                            "('BUSINESS_STAFF'::character varying)::text" +
                            "])))"
                        );
                        
                        logger.info("Successfully updated users_role_check constraint");
                    } else {
                        logger.info("Constraint already includes PLATFORM_ADMIN, skipping migration");
                    }
                } catch (Exception e) {
                    // If we can't check, just try to update it
                    logger.warn("Could not verify constraint, attempting update: {}", e.getMessage());
                    try {
                        jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
                        jdbcTemplate.execute(
                            "ALTER TABLE users ADD CONSTRAINT users_role_check " +
                            "CHECK (((role)::text = ANY (ARRAY[" +
                            "('PLATFORM_ADMIN'::character varying)::text, " +
                            "('BUSINESS_OWNER'::character varying)::text, " +
                            "('BUSINESS_STAFF'::character varying)::text" +
                            "])))"
                        );
                        logger.info("Successfully updated constraint");
                    } catch (Exception ex) {
                        logger.error("Failed to update constraint: {}", ex.getMessage());
                    }
                }
            } else {
                logger.info("users_role_check constraint not found, creating it");
                
                // Create the constraint if it doesn't exist (without BUSINESS_MANAGER)
                jdbcTemplate.execute(
                    "ALTER TABLE users ADD CONSTRAINT users_role_check " +
                    "CHECK (((role)::text = ANY (ARRAY[" +
                    "('PLATFORM_ADMIN'::character varying)::text, " +
                    "('BUSINESS_OWNER'::character varying)::text, " +
                    "('BUSINESS_STAFF'::character varying)::text" +
                    "])))"
                );
                logger.info("Successfully created users_role_check constraint");
            }
            
        } catch (Exception e) {
            logger.error("Error during database migration: {}", e.getMessage(), e);
            logger.error("Migration failed. Please run the SQL migration manually. See MIGRATION_INSTRUCTIONS.md");
            // Don't throw - allow application to continue if migration fails
            // User can run manual migration if needed
        }
    }
}


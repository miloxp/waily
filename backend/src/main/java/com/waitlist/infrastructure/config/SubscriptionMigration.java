package com.waitlist.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Order(3) // Run after RemoveBusinessManagerMigration
public class SubscriptionMigration {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            logger.info("Running database migration: Adding subscription support");

            // Check if subscription_plan enum exists
            try {
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pg_type WHERE typname = 'subscription_plan'",
                    Integer.class
                );
                logger.info("subscription_plan enum already exists");
            } catch (Exception e) {
                logger.info("Creating subscription_plan enum");
                jdbcTemplate.execute(
                    "DO $$ BEGIN " +
                    "CREATE TYPE subscription_plan AS ENUM ('BASIC', 'PRO', 'ENTERPRISE'); " +
                    "EXCEPTION WHEN duplicate_object THEN null; " +
                    "END $$;"
                );
            }

            // Check if subscription_status enum exists
            try {
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pg_type WHERE typname = 'subscription_status'",
                    Integer.class
                );
                logger.info("subscription_status enum already exists");
            } catch (Exception e) {
                logger.info("Creating subscription_status enum");
                jdbcTemplate.execute(
                    "DO $$ BEGIN " +
                    "CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'TRIAL', 'EXPIRED', 'CANCELLED', 'SUSPENDED'); " +
                    "EXCEPTION WHEN duplicate_object THEN null; " +
                    "END $$;"
                );
            }

            // Check if subscriptions table exists
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name = 'subscriptions'",
                Integer.class
            );

            if (tableExists == null || tableExists == 0) {
                logger.info("Creating subscriptions table");
                jdbcTemplate.execute(
                    "CREATE TABLE subscriptions (" +
                    "id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), " +
                    "business_id UUID NOT NULL UNIQUE REFERENCES businesses(id) ON DELETE CASCADE, " +
                    "plan subscription_plan NOT NULL DEFAULT 'BASIC', " +
                    "status subscription_status NOT NULL DEFAULT 'TRIAL', " +
                    "start_date DATE NOT NULL, " +
                    "end_date DATE, " +
                    "billing_cycle_days INTEGER NOT NULL DEFAULT 30, " +
                    "monthly_price DECIMAL(10, 2) NOT NULL DEFAULT 0, " +
                    "auto_renew BOOLEAN NOT NULL DEFAULT true, " +
                    "trial_end_date DATE, " +
                    "notes TEXT, " +
                    "created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );

                // Create indexes
                jdbcTemplate.execute(
                    "CREATE INDEX idx_subscriptions_business ON subscriptions(business_id)"
                );
                jdbcTemplate.execute(
                    "CREATE INDEX idx_subscriptions_status ON subscriptions(status)"
                );
                jdbcTemplate.execute(
                    "CREATE INDEX idx_subscriptions_plan ON subscriptions(plan)"
                );

                // Create trigger
                jdbcTemplate.execute(
                    "CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions " +
                    "FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()"
                );

                logger.info("Successfully created subscriptions table and indexes");
            } else {
                logger.info("subscriptions table already exists");
            }

        } catch (Exception e) {
            logger.error("Error during subscription migration: {}", e.getMessage(), e);
            logger.error("Migration failed. Please run the SQL migration manually.");
            // Don't throw - allow application to continue if migration fails
        }
    }
}


package com.waitlist.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Order(6) // Run after other migrations
public class CustomerBusinessMigration {

    private static final Logger logger = LoggerFactory.getLogger(CustomerBusinessMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        try {
            logger.info("Running database migration: Creating customer_businesses join table");

            // Check if customer_businesses table exists
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name = 'customer_businesses'",
                Integer.class
            );

            if (tableExists == null || tableExists == 0) {
                logger.info("Creating customer_businesses join table");

                // Create the join table
                jdbcTemplate.execute(
                    "CREATE TABLE customer_businesses (" +
                    "customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE, " +
                    "business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE, " +
                    "PRIMARY KEY (customer_id, business_id)" +
                    ")"
                );

                // Create indexes
                jdbcTemplate.execute(
                    "CREATE INDEX idx_customer_businesses_customer ON customer_businesses(customer_id)"
                );
                jdbcTemplate.execute(
                    "CREATE INDEX idx_customer_businesses_business ON customer_businesses(business_id)"
                );

                // Migrate existing data: associate customers with businesses through reservations and waitlist
                logger.info("Migrating existing customer-business relationships from reservations and waitlist");
                jdbcTemplate.execute(
                    "INSERT INTO customer_businesses (customer_id, business_id) " +
                    "SELECT DISTINCT r.customer_id, r.business_id FROM reservations r " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM customer_businesses cb " +
                    "  WHERE cb.customer_id = r.customer_id AND cb.business_id = r.business_id" +
                    ")"
                );
                jdbcTemplate.execute(
                    "INSERT INTO customer_businesses (customer_id, business_id) " +
                    "SELECT DISTINCT w.customer_id, w.business_id FROM waitlist_entries w " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM customer_businesses cb " +
                    "  WHERE cb.customer_id = w.customer_id AND cb.business_id = w.business_id" +
                    ")"
                );

                logger.info("Migration completed successfully. customer_businesses table created and populated.");
            } else {
                logger.info("customer_businesses table already exists");
            }

        } catch (Exception e) {
            logger.error("Error during customer-business migration: {}", e.getMessage(), e);
            logger.error("Migration failed. Please run the SQL migration manually.");
            // Don't throw - allow application to continue if migration fails
        }
    }
}


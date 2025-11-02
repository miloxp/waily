# Migration Instructions - Add PLATFORM_ADMIN Role

The database schema needs to be updated to include the new `PLATFORM_ADMIN` role.

## Option 1: Run SQL Migration Script

Execute the migration script directly on your PostgreSQL database:

```bash
psql -h localhost -U waitlist_user -d waitlist_db -f backend/src/main/resources/db/migration_add_platform_admin.sql
```

Or connect to your database and run:

```sql
-- Update the check constraint on users table to include PLATFORM_ADMIN
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users ADD CONSTRAINT users_role_check 
CHECK (((role)::text = ANY (ARRAY[
    ('PLATFORM_ADMIN'::character varying)::text,
    ('BUSINESS_OWNER'::character varying)::text,
    ('BUSINESS_MANAGER'::character varying)::text,
    ('BUSINESS_STAFF'::character varying)::text
])));
```

## Option 2: Manual SQL Execution

1. Connect to your PostgreSQL database:
   ```bash
   psql -h localhost -U waitlist_user -d waitlist_db
   ```

2. Run these commands:
   ```sql
   ALTER TABLE users DROP CONSTRAINT users_role_check;
   
   ALTER TABLE users ADD CONSTRAINT users_role_check 
   CHECK (((role)::text = ANY (ARRAY[
       ('PLATFORM_ADMIN'::character varying)::text,
       ('BUSINESS_OWNER'::character varying)::text,
       ('BUSINESS_MANAGER'::character varying)::text,
       ('BUSINESS_STAFF'::character varying)::text
   ])));
   ```

## Option 3: Using pgAdmin or Database GUI

1. Open your database management tool (pgAdmin, DBeaver, etc.)
2. Connect to your `waitlist_db` database
3. Open a SQL query window
4. Paste and execute the SQL from Option 2

## Verify Migration

After running the migration, verify it worked:

```sql
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'users' AND constraint_name = 'users_role_check';
```

You should see the constraint listed.

## After Migration

Once the migration is complete, restart your Spring Boot application. The `DataInitializer` will automatically create all demo users including the PLATFORM_ADMIN user.


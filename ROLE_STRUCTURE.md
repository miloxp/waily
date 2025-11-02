# Role Structure - B2B SaaS Platform

This document outlines the role-based access control (RBAC) structure for the Waitlist & Reservations B2B SaaS platform.

## Role Hierarchy

```
PLATFORM_ADMIN (Platform Owner)
    └── Manages all businesses, subscriptions, and platform-wide reports
    
BUSINESS_OWNER (Business Client Owner)
    └── Manages their own business account
    
BUSINESS_MANAGER (Business Manager)
    └── Manages operations for their business
    
BUSINESS_STAFF (Business Staff)
    └── Basic operational access
```

---

## PLATFORM_ADMIN

**Purpose**: Platform owner who manages the entire SaaS application and all client businesses.

### Platform Management
- ✅ **Create business accounts** for new clients (`POST /api/business`)
- ✅ **Delete/deactivate business accounts** (`DELETE /api/business/{id}`)
- ✅ **View all businesses** across the platform
- ✅ **Access all business data** (bypasses business scoping)
- ✅ **Manage subscriptions** for client businesses (future feature)
- ✅ **View platform-wide reports and analytics** (future feature)

### Business Operations (Cross-Business Access)
- ✅ View all reservations across all businesses
- ✅ View all waitlist entries across all businesses
- ✅ View all customers across all businesses
- ✅ Access business-specific operations for any business
- ✅ Confirm/complete/cancel reservations for any business
- ✅ Notify/seat customers for any business

### Restrictions
- ❌ Cannot be restricted by business scoping (can access all businesses)
- ❌ Should not manage day-to-day operations (delegated to BUSINESS_OWNER)

### Use Cases
- Onboarding new business clients
- Managing subscriptions and billing
- Generating platform-wide analytics
- Troubleshooting issues across businesses
- Deactivating business accounts

---

## BUSINESS_OWNER

**Purpose**: Owner of a business client account who manages their own business.

### Business Management (Own Business Only)
- ✅ **View own business** details
- ✅ **Update own business** information (`PUT /api/business/{id}`)
- ✅ **Delete/deactivate own business** (`DELETE /api/business/{id}`)
- ✅ **View own business reports** (future feature)

### Business Operations (Own Business Only)
- ✅ View all reservations for own business
- ✅ Create reservations for own business
- ✅ Confirm reservations for own business
- ✅ Complete reservations for own business
- ✅ Cancel reservations for own business

### Waitlist Management (Own Business Only)
- ✅ View waitlist for own business
- ✅ Add customers to own business waitlist
- ✅ Notify customers for own business
- ✅ Seat customers for own business
- ✅ Update waitlist status for own business
- ✅ Remove customers from own business waitlist

### Customer Management
- ✅ View all customers
- ✅ Create customers
- ✅ Update customers
- ✅ Search customers

### Restrictions
- ❌ **Cannot create other businesses** (restricted to PLATFORM_ADMIN)
- ❌ Cannot access other businesses' data
- ❌ Cannot view platform-wide reports
- ❌ Cannot manage subscriptions

### Use Cases
- Managing their restaurant/service business
- Viewing their business performance
- Managing their staff (BUSINESS_MANAGER, BUSINESS_STAFF)
- Operating their waitlist and reservations

---

## BUSINESS_MANAGER

**Purpose**: Manager role for day-to-day operations of a business.

### Business Operations (Own Business Only)
- ✅ View reservations for own business
- ✅ Create reservations for own business
- ✅ Cancel reservations for own business
- ❌ Cannot confirm reservations (BUSINESS_OWNER only)
- ❌ Cannot complete reservations (BUSINESS_OWNER only)

### Waitlist Management (Own Business Only)
- ✅ View waitlist for own business
- ✅ Add customers to own business waitlist
- ✅ Update waitlist status for own business
- ✅ Remove customers from own business waitlist
- ❌ Cannot notify customers (BUSINESS_OWNER only)
- ❌ Cannot seat customers (BUSINESS_OWNER only)

### Customer Management
- ✅ View all customers
- ✅ Create customers
- ✅ Update customers
- ✅ Search customers

### Restrictions
- ❌ Cannot create/delete businesses
- ❌ Cannot view all reservations (cross-business)
- ❌ Cannot confirm/complete reservations
- ❌ Cannot notify/seat waitlist customers
- ❌ Cannot manage business settings
- ❌ Cannot view business reports

### Use Cases
- Day-to-day waitlist management
- Creating and managing reservations
- Managing customer information
- Overseeing staff operations

---

## BUSINESS_STAFF

**Purpose**: Basic operational staff with limited permissions.

### Business Operations (Own Business Only)
- ✅ View reservations for own business (read-only access to own business)
- ✅ Create reservations for own business
- ✅ Cancel reservations for own business
- ❌ Cannot confirm reservations
- ❌ Cannot complete reservations

### Waitlist Management (Own Business Only)
- ✅ View waitlist for own business
- ✅ Add customers to own business waitlist
- ✅ Update waitlist status for own business (limited)
- ✅ Remove customers from own business waitlist
- ❌ Cannot notify customers
- ❌ Cannot seat customers

### Customer Management
- ✅ View all customers
- ✅ Create customers
- ✅ Update customers
- ✅ Search customers

### Restrictions
- ❌ Cannot create/delete businesses
- ❌ Cannot view all reservations (cross-business)
- ❌ Cannot confirm/complete reservations
- ❌ Cannot notify/seat waitlist customers
- ❌ Cannot manage business settings
- ❌ Cannot view reports

### Use Cases
- Basic customer service
- Adding customers to waitlist
- Creating reservations
- Basic customer management

---

## Permission Matrix

| Feature | PLATFORM_ADMIN | BUSINESS_OWNER | BUSINESS_MANAGER | BUSINESS_STAFF |
|---------|---------------|----------------|------------------|----------------|
| **Platform Management** |
| Create Business Account | ✅ | ❌ | ❌ | ❌ |
| Delete Business Account | ✅ | ✅ (own) | ❌ | ❌ |
| View All Businesses | ✅ | ✅ | ✅ | ✅ |
| Manage Subscriptions | ✅ | ❌ | ❌ | ❌ |
| Platform Reports | ✅ | ❌ | ❌ | ❌ |
| **Business Operations** |
| View All Reservations | ✅ (all) | ✅ (own) | ❌ | ❌ |
| Create Reservations | ✅ (all) | ✅ (own) | ✅ (own) | ✅ (own) |
| Confirm Reservations | ✅ (all) | ✅ (own) | ❌ | ❌ |
| Complete Reservations | ✅ (all) | ✅ (own) | ❌ | ❌ |
| Cancel Reservations | ✅ (all) | ✅ (own) | ✅ (own) | ✅ (own) |
| **Waitlist Operations** |
| View Waitlist | ✅ (all) | ✅ (own) | ✅ (own) | ✅ (own) |
| Add to Waitlist | ✅ (all) | ✅ (own) | ✅ (own) | ✅ (own) |
| Notify Customers | ✅ (all) | ✅ (own) | ❌ | ❌ |
| Seat Customers | ✅ (all) | ✅ (own) | ❌ | ❌ |
| Update Waitlist Status | ✅ (all) | ✅ (own) | ✅ (own) | ✅ (own) |
| **Customer Management** |
| View Customers | ✅ (all) | ✅ | ✅ | ✅ |
| Create Customers | ✅ | ✅ | ✅ | ✅ |
| Update Customers | ✅ | ✅ | ✅ | ✅ |

---

## Future Features to Implement

### For PLATFORM_ADMIN:
1. **Subscription Management**
   - View all business subscriptions
   - Update subscription plans
   - Handle billing and payments
   - Manage subscription status (active, suspended, cancelled)

2. **Platform Analytics & Reports**
   - Total businesses registered
   - Active vs inactive businesses
   - Revenue analytics
   - Usage metrics per business
   - Platform-wide reservation/waitlist statistics

3. **Business Account Management**
   - Onboarding workflow
   - Account activation/deactivation
   - Trial period management
   - Feature flags per business

### For BUSINESS_OWNER:
1. **Business Reports**
   - Reservation analytics
   - Waitlist performance
   - Customer analytics
   - Revenue reports
   - Peak hours analysis

2. **Subscription Management**
   - View own subscription
   - Upgrade/downgrade plan
   - View billing history
   - Payment methods

---

## Implementation Notes

### Database Considerations
- `PLATFORM_ADMIN` users may need a special business_id (e.g., a "Platform" business) or nullable business_id
- Consider adding a `subscription` table for business subscriptions
- Add `platform_reports` or `analytics` tables for cross-business reporting

### Authorization Patterns
- PLATFORM_ADMIN: Skip business_id filtering in queries
- BUSINESS_OWNER/MANAGER/STAFF: Filter by authenticated user's business_id
- Use `@PreAuthorize` annotations for role-based access control
- Use business scoping in service/repository layers for data isolation

### Security Considerations
- PLATFORM_ADMIN should be created manually or through a secure setup process
- Never allow BUSINESS_OWNER to create other businesses
- Always validate business ownership before allowing access to business data
- Implement audit logging for PLATFORM_ADMIN actions


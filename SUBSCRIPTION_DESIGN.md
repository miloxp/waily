# Subscription Design Decision

## Current Implementation: **Business-Level Subscriptions** ✅

### Architecture
- **Subscription → Business** (OneToOne relationship)
- Each business has its own subscription
- Each business can have different plans independently

### Advantages

#### 1. **Business Autonomy**
- Each business can choose its own plan based on needs
- Restaurant A can have PRO while Restaurant B has BASIC
- Businesses are independently billable units

#### 2. **Flexibility for Multi-Business Owners**
```
Owner owns 3 businesses:
├── Restaurant A → PRO ($99/mo)
├── Restaurant B → BASIC ($29/mo)  
└── Cafe C → TRIAL (free)

Each can upgrade/downgrade independently
```

#### 3. **Business Transfer**
- When ownership transfers, subscription stays with the business
- New owner inherits the subscription
- No subscription migration needed

#### 4. **Feature Limits per Business**
- Capacity limits apply per business
- Reservation limits apply per business
- Feature access is scoped to business level

#### 5. **Clearer Billing & Accounting**
- Each business = separate invoice
- Easier accounting for franchises
- Clear revenue attribution per location

### Disadvantages

#### 1. **Billing Complexity for Multi-Business Owners**
- Owner with 10 businesses gets 10 separate invoices
- Harder to offer "bundle" pricing

#### 2. **More Subscriptions to Manage**
- PLATFORM_ADMIN must manage subscription per business
- More records to track

---

## Alternative: **User-Level Subscriptions**

### Architecture (Not Implemented)
- **Subscription → User** (OneToOne relationship)
- Each user has one subscription covering all their businesses

### Advantages

#### 1. **Simplified Billing**
- One invoice per user
- Easier to offer bundle pricing ("Own 5 businesses for $200/mo")

#### 2. **Easier for Platform Admin**
- Fewer subscription records
- Simpler billing workflows

### Disadvantages

#### 1. **Less Flexible**
- All businesses under one user share the same plan
- Can't have PRO for one business and BASIC for another

#### 2. **Business Transfer Issues**
- When business transfers ownership, subscription logic becomes complex
- Who pays for the subscription? Old owner or new owner?

#### 3. **Scaling Issues**
- If user adds a 6th business, do they need to upgrade?
- What if different businesses need different features?

#### 4. **Billing Attribution**
- Harder to attribute revenue to specific businesses
- Accounting complexity for multi-location businesses

---

## **Recommended Approach: Business-Level with User Discounts** 🎯

### Current Implementation + Enhancements

**Keep subscriptions at Business level** but add:

#### 1. **User-Level Discounts** (Future Feature)
```sql
CREATE TABLE user_subscription_discounts (
    user_id UUID REFERENCES users(id),
    discount_percentage DECIMAL(5,2),
    discount_type VARCHAR(50), -- 'VOLUME', 'LOYALTY', 'PARTNER'
    applicable_business_count INT
);
```

#### 2. **Multi-Business Pricing Tiers**
- Businesses 1-2: Regular pricing
- Businesses 3-5: 10% discount per business
- Businesses 6+: 20% discount per business

#### 3. **Unified Billing View** (Frontend Feature)
- Show all subscriptions for a user in one place
- "My Subscriptions" dashboard showing all businesses
- Single invoice view combining all business subscriptions

### Implementation Example

```java
// Business-level subscription (current)
Subscription restaurantA = new Subscription(restaurantA, Plan.PRO, ...);

// User-level discount (future)
UserDiscount userDiscount = new UserDiscount(
    user, 
    DiscountType.VOLUME, 
    15.0, // 15% off if user owns 3+ businesses
    3    // minimum 3 businesses
);
```

---

## Decision Matrix

| Factor | Business-Level | User-Level | Winner |
|--------|----------------|------------|--------|
| Flexibility | ✅ Each business independent | ❌ All share same plan | **Business** |
| Billing Clarity | ✅ Per-business invoices | ⚠️ One invoice, unclear attribution | **Business** |
| Multi-Business Owners | ⚠️ Multiple subscriptions | ✅ One subscription | **User** |
| Business Transfer | ✅ Subscription stays with business | ❌ Complex transfer logic | **Business** |
| Feature Limits | ✅ Per-business limits | ❌ Shared limits across all | **Business** |
| Billing Complexity | ⚠️ More subscriptions | ✅ Fewer subscriptions | **User** |
| Platform Admin | ⚠️ More records | ✅ Fewer records | **User** |

**Winner: Business-Level** (5 wins vs 3 wins)

---

## Final Recommendation

✅ **Keep subscriptions attached to Business**

### Reasoning:
1. **B2B SaaS Best Practice**: Businesses are the customers, not users
2. **Future-Proof**: Works better when businesses transfer ownership
3. **Feature Gating**: Subscription limits naturally apply per business
4. **Scalability**: Each business can scale independently

### Enhancements to Consider:
1. **Add user-level discounts** for volume (multi-business owners)
2. **Create unified billing dashboard** for users with multiple businesses
3. **Add bundle pricing options** in subscription plans
4. **Implement subscription groups** for franchise accounts

---

## Migration Path (if changing)

If you ever want to switch to user-level subscriptions:

1. **Create migration script** to aggregate business subscriptions
2. **Handle conflicts** (user with businesses on different plans)
3. **Update all controllers** to check user subscription instead of business
4. **Update frontend** to show user-level subscription status

**Cost**: High
**Benefit**: Low (business-level is better for B2B SaaS)
**Recommendation**: Don't migrate


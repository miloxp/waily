package com.waitlist.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class BusinessTest {

    private Business business;

    @BeforeEach
    void setUp() {
        business = new Business(
                "Test Restaurant",
                BusinessType.RESTAURANT,
                "123 Main St",
                "+1234567890",
                "test@restaurant.com",
                50,
                60);
    }

    @Test
    void testBusinessCreation() {
        assertNotNull(business);
        assertEquals("Test Restaurant", business.getName());
        assertEquals(BusinessType.RESTAURANT, business.getType());
        assertEquals("123 Main St", business.getAddress());
        assertEquals("+1234567890", business.getPhone());
        assertEquals("test@restaurant.com", business.getEmail());
        assertEquals(50, business.getCapacity());
        assertEquals(60, business.getAverageServiceTime());
        assertTrue(business.getIsActive());
    }

    @Test
    void testCanAccommodate() {
        assertTrue(business.canAccommodate(25));
        assertTrue(business.canAccommodate(50));
        assertFalse(business.canAccommodate(51));

        business.deactivate();
        assertFalse(business.canAccommodate(25));
    }

    @Test
    void testDeactivate() {
        assertTrue(business.getIsActive());
        business.deactivate();
        assertFalse(business.getIsActive());
    }

    @Test
    void testActivate() {
        business.deactivate();
        assertFalse(business.getIsActive());
        business.activate();
        assertTrue(business.getIsActive());
    }
}


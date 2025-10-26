package com.waitlist.infrastructure.service;

import com.waitlist.domain.service.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwilioSmsServiceTest {

    @Mock
    private SmsService smsService;

    @InjectMocks
    private TwilioSmsService twilioSmsService;

    @Test
    void testSendSms_WithValidCredentials() {
        // Set up test data
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "test-account-sid");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "test-auth-token");
        ReflectionTestUtils.setField(twilioSmsService, "twilioPhoneNumber", "+1234567890");

        // Mock the SMS service behavior
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);

        // Test the method
        boolean result = twilioSmsService.sendSms("+1987654321", "Test message");

        // Verify the result
        assertTrue(result);
    }

    @Test
    void testSendWaitlistNotification() {
        // Set up test data
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "test-account-sid");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "test-auth-token");
        ReflectionTestUtils.setField(twilioSmsService, "twilioPhoneNumber", "+1234567890");

        // Mock the SMS service behavior
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);

        // Test the method
        boolean result = twilioSmsService.sendWaitlistNotification(
                "+1987654321",
                "Test Restaurant",
                30,
                5);

        // Verify the result
        assertTrue(result);
    }

    @Test
    void testSendTableReadyNotification() {
        // Set up test data
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "test-account-sid");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "test-auth-token");
        ReflectionTestUtils.setField(twilioSmsService, "twilioPhoneNumber", "+1234567890");

        // Mock the SMS service behavior
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);

        // Test the method
        boolean result = twilioSmsService.sendTableReadyNotification(
                "+1987654321",
                "Test Restaurant",
                "+1234567890");

        // Verify the result
        assertTrue(result);
    }

    @Test
    void testSendSms_WithInvalidCredentials() {
        // Set up test data with empty credentials
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "");
        ReflectionTestUtils.setField(twilioSmsService, "twilioPhoneNumber", "+1234567890");

        // Test the method
        boolean result = twilioSmsService.sendSms("+1987654321", "Test message");

        // Verify the result
        assertFalse(result);
    }
}


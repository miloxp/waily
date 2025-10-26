package com.waitlist.infrastructure.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.waitlist.domain.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "sms.mock.enabled", havingValue = "false")
public class TwilioSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && authToken != null && !accountSid.isEmpty() && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio SMS service initialized successfully");
        } else {
            logger.warn("Twilio credentials not configured. SMS functionality will be disabled.");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        try {
            if (accountSid == null || authToken == null || accountSid.isEmpty() || authToken.isEmpty()) {
                logger.warn("Twilio not configured. SMS not sent to {}", phoneNumber);
                return false;
            }

            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message).create();

            logger.info("SMS sent successfully to {}", phoneNumber);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendWaitlistNotification(String phoneNumber, String businessName,
            Integer estimatedWaitTime, Integer position) {
        String message = String.format(
                "Hi! You're #%d on the waitlist at %s. " +
                        "Estimated wait time: %d minutes. " +
                        "We'll text you when your table is ready!",
                position, businessName, estimatedWaitTime);

        return sendSms(phoneNumber, message);
    }

    @Override
    public boolean sendTableReadyNotification(String phoneNumber, String businessName, String businessPhone) {
        String message = String.format(
                "Your table is ready at %s! Please come to the host stand. " +
                        "If you have any questions, call us at %s. " +
                        "You have 15 minutes to claim your table.",
                businessName, businessPhone);

        return sendSms(phoneNumber, message);
    }

    @Override
    public boolean sendReservationConfirmation(String phoneNumber, String businessName,
            String reservationDate, String reservationTime,
            Integer partySize) {
        String message = String.format(
                "Reservation confirmed at %s for %s at %s for %d people. " +
                        "We look forward to seeing you!",
                businessName, reservationDate, reservationTime, partySize);

        return sendSms(phoneNumber, message);
    }

    @Override
    public boolean sendReservationReminder(String phoneNumber, String businessName,
            String reservationDate, String reservationTime) {
        String message = String.format(
                "Reminder: You have a reservation at %s tomorrow (%s) at %s. " +
                        "Please arrive 5 minutes early. We look forward to seeing you!",
                businessName, reservationDate, reservationTime);

        return sendSms(phoneNumber, message);
    }
}

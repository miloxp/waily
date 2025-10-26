package com.waitlist.infrastructure.service;

import com.waitlist.domain.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "sms.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(MockSmsService.class);

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        logger.info("MOCK SMS to {}: {}", phoneNumber, message);
        return true;
    }

    @Override
    public boolean sendWaitlistNotification(String phoneNumber, String businessName,
            Integer estimatedWaitTime, Integer position) {
        String message = String.format(
                "Hi! You're #%d on the waitlist at %s. " +
                        "Estimated wait time: %d minutes. " +
                        "We'll text you when your table is ready!",
                position, businessName, estimatedWaitTime);

        logger.info("MOCK Waitlist Notification to {}: {}", phoneNumber, message);
        return true;
    }

    @Override
    public boolean sendTableReadyNotification(String phoneNumber, String businessName, String businessPhone) {
        String message = String.format(
                "Your table is ready at %s! Please come to the host stand. " +
                        "If you have any questions, call us at %s. " +
                        "You have 15 minutes to claim your table.",
                businessName, businessPhone);

        logger.info("MOCK Table Ready Notification to {}: {}", phoneNumber, message);
        return true;
    }

    @Override
    public boolean sendReservationConfirmation(String phoneNumber, String businessName,
            String reservationDate, String reservationTime,
            Integer partySize) {
        String message = String.format(
                "Reservation confirmed at %s for %s at %s for %d people. " +
                        "We look forward to seeing you!",
                businessName, reservationDate, reservationTime, partySize);

        logger.info("MOCK Reservation Confirmation to {}: {}", phoneNumber, message);
        return true;
    }

    @Override
    public boolean sendReservationReminder(String phoneNumber, String businessName,
            String reservationDate, String reservationTime) {
        String message = String.format(
                "Reminder: You have a reservation at %s tomorrow (%s) at %s. " +
                        "Please arrive 5 minutes early. We look forward to seeing you!",
                businessName, reservationDate, reservationTime);

        logger.info("MOCK Reservation Reminder to {}: {}", phoneNumber, message);
        return true;
    }
}


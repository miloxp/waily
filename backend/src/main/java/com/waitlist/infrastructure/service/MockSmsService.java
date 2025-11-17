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

        public MockSmsService() {
                logger.info("MockSmsService initialized - SMS will be logged but not sent");
        }

        @Override
        public boolean sendSms(String phoneNumber, String message) {
                logger.info("MOCK SMS to {}: {}", phoneNumber, message);
                return true;
        }

        @Override
        public boolean sendWaitlistNotification(String phoneNumber, String businessName,
                        Integer estimatedWaitTime, Integer position) {
                String message = String.format(
                                "Posición #%d en %s. Espera: %d min. Te avisamos cuando esté lista.",
                                position, businessName, estimatedWaitTime);

                logger.info("MOCK Waitlist Notification to {}: {}", phoneNumber, message);
                return true;
        }

        @Override
        public boolean sendTableReadyNotification(String phoneNumber, String businessName, String businessPhone) {
                String message = String.format(
                                "¡Mesa lista en %s! Acércate a recepción. Tienes 15 min. Tel: %s",
                                businessName, businessPhone);

                logger.info("MOCK Table Ready Notification to {}: {}", phoneNumber, message);
                return true;
        }

        @Override
        public boolean sendReservationConfirmation(String phoneNumber, String businessName,
                        String reservationDate, String reservationTime,
                        Integer partySize) {
                String message = String.format(
                                "Reservación confirmada en %s: %s a las %s para %d persona%s. ¡Te esperamos!",
                                businessName, reservationDate, reservationTime, partySize,
                                partySize == 1 ? "" : "s");

                logger.info("MOCK Reservation Confirmation to {}: {}", phoneNumber, message);
                return true;
        }

        @Override
        public boolean sendReservationReminder(String phoneNumber, String businessName,
                        String reservationDate, String reservationTime) {
                String message = String.format(
                                "Recordatorio: Reservación en %s mañana (%s) a las %s. Llega 5 min antes.",
                                businessName, reservationDate, reservationTime);

                logger.info("MOCK Reservation Reminder to {}: {}", phoneNumber, message);
                return true;
        }
}

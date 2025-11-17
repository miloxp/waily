package com.waitlist.infrastructure.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
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
        logger.info("TwilioSmsService initialization started");
        logger.debug("Account SID configured: {}", accountSid != null && !accountSid.isEmpty());
        logger.debug("Auth Token configured: {}", authToken != null && !authToken.isEmpty());
        logger.debug("Phone Number configured: {}", twilioPhoneNumber != null && !twilioPhoneNumber.isEmpty());

        if (accountSid != null && authToken != null && !accountSid.isEmpty() && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio SMS service initialized successfully with phone number: {}", twilioPhoneNumber);
        } else {
            logger.warn("Twilio credentials not configured. SMS functionality will be disabled.");
            logger.warn("Account SID: {}, Auth Token: {}",
                    accountSid != null && !accountSid.isEmpty() ? "configured" : "missing",
                    authToken != null && !authToken.isEmpty() ? "configured" : "missing");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        try {
            if (accountSid == null || authToken == null || accountSid.isEmpty() || authToken.isEmpty()) {
                logger.warn("Twilio not configured. SMS not sent to {}", phoneNumber);
                return false;
            }

            if (twilioPhoneNumber == null || twilioPhoneNumber.isEmpty() ||
                    twilioPhoneNumber.equals("your-twilio-phone-number")) {
                logger.error("Twilio phone number not configured. Please set TWILIO_PHONE_NUMBER in env file");
                return false;
            }

            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message).create();

            logger.info("SMS sent successfully to {} from {}, message: {}", phoneNumber, twilioPhoneNumber, message);
            return true;

        } catch (ApiException e) {
            String errorMessage = e.getMessage();
            logger.error("Twilio API error sending SMS to {}: {}", phoneNumber, errorMessage);

            // Provide helpful error messages for common issues
            if (errorMessage != null) {
                if (errorMessage.contains("not a valid message-capable")) {
                    logger.error(
                            "The Twilio phone number {} may not be activated for SMS or may not support international messaging to {}. "
                                    +
                                    "Check your Twilio console: https://console.twilio.com/us1/develop/phone-numbers/manage/incoming",
                            twilioPhoneNumber, phoneNumber);
                } else if (errorMessage.contains("unverified")) {
                    logger.error(
                            "This appears to be a Twilio trial account. Trial accounts can only send SMS to verified phone numbers. "
                                    +
                                    "Verify the number at: https://console.twilio.com/us1/develop/phone-numbers/manage/verified");
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendWaitlistNotification(String phoneNumber, String businessName,
            Integer estimatedWaitTime, Integer position) {
        String message = String.format(
                "Posición #%d en %s. Espera: %d min. Te avisamos cuando esté lista.",
                position, businessName, estimatedWaitTime);

        return sendSms(phoneNumber, message);
    }

    @Override
    public boolean sendTableReadyNotification(String phoneNumber, String businessName, String businessPhone) {
        String message = String.format(
                "¡Mesa lista en %s! Acércate a recepción. Tienes 15 min. Tel: %s",
                businessName, businessPhone);

        return sendSms(phoneNumber, message);
    }

    @Override
    public boolean sendReservationConfirmation(String phoneNumber, String businessName,
            String reservationDate, String reservationTime,
            Integer partySize) {
        String message = String.format(
                "Reservación confirmada en %s: %s a las %s para %d persona%s. ¡Te esperamos!",
                businessName, reservationDate, reservationTime, partySize,
                partySize == 1 ? "" : "s");

        return sendSms(phoneNumber, message);
    }

    @Override
    public boolean sendReservationReminder(String phoneNumber, String businessName,
            String reservationDate, String reservationTime) {
        String message = String.format(
                "Recordatorio: Reservación en %s mañana (%s) a las %s. Llega 5 min antes.",
                businessName, reservationDate, reservationTime);

        return sendSms(phoneNumber, message);
    }
}

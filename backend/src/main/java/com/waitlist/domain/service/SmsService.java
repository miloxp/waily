package com.waitlist.domain.service;

public interface SmsService {

    /**
     * Send an SMS message to the specified phone number
     * 
     * @param phoneNumber The recipient's phone number
     * @param message     The message content
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);

    /**
     * Send a waitlist notification to a customer
     * 
     * @param phoneNumber       The customer's phone number
     * @param businessName      The name of the business
     * @param estimatedWaitTime The estimated wait time in minutes
     * @param position          The customer's position in the waitlist
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendWaitlistNotification(String phoneNumber, String businessName,
            Integer estimatedWaitTime, Integer position);

    /**
     * Send a table ready notification to a customer
     * 
     * @param phoneNumber   The customer's phone number
     * @param businessName  The name of the business
     * @param businessPhone The business phone number for contact
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendTableReadyNotification(String phoneNumber, String businessName, String businessPhone);

    /**
     * Send a reservation confirmation to a customer
     * 
     * @param phoneNumber     The customer's phone number
     * @param businessName    The name of the business
     * @param reservationDate The reservation date
     * @param reservationTime The reservation time
     * @param partySize       The party size
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendReservationConfirmation(String phoneNumber, String businessName,
            String reservationDate, String reservationTime,
            Integer partySize);

    /**
     * Send a reservation reminder to a customer
     * 
     * @param phoneNumber     The customer's phone number
     * @param businessName    The name of the business
     * @param reservationDate The reservation date
     * @param reservationTime The reservation time
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendReservationReminder(String phoneNumber, String businessName,
            String reservationDate, String reservationTime);
}


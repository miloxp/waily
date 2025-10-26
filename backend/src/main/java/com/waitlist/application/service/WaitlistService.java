package com.waitlist.application.service;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.Customer;
import com.waitlist.domain.entity.WaitlistEntry;
import com.waitlist.domain.service.SmsService;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.CustomerRepository;
import com.waitlist.infrastructure.repository.WaitlistEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WaitlistService {

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SmsService smsService;

    public WaitlistEntry addCustomerToWaitlist(UUID businessId, UUID customerId, Integer partySize) {
        // Validate business exists and is active
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        if (!business.getIsActive()) {
            throw new IllegalArgumentException("Business is not active");
        }

        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Check if customer is already on the waitlist
        if (waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId).isPresent()) {
            throw new IllegalStateException("Customer is already on the waitlist");
        }

        // Get next position
        Integer nextPosition = waitlistEntryRepository.findMaxPositionForBusiness(businessId)
                .orElse(0) + 1;

        // Create waitlist entry
        WaitlistEntry entry = new WaitlistEntry(business, customer, partySize, nextPosition);

        // Calculate estimated wait time
        entry.calculateEstimatedWaitTime(business.getAverageServiceTime(), nextPosition);

        // Save entry
        WaitlistEntry savedEntry = waitlistEntryRepository.save(entry);

        // Send SMS notification
        try {
            smsService.sendWaitlistNotification(
                    customer.getPhone(),
                    business.getName(),
                    savedEntry.getEstimatedWaitTime(),
                    savedEntry.getPosition());
        } catch (Exception e) {
            // Log error but don't fail the operation
            // In a real application, you might want to retry or queue the SMS
        }

        return savedEntry;
    }

    public void notifyCustomer(UUID entryId) {
        WaitlistEntry entry = waitlistEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (entry.canBeNotified()) {
            entry.notifyCustomer();
            waitlistEntryRepository.save(entry);

            // Send SMS notification
            smsService.sendTableReadyNotification(
                    entry.getCustomer().getPhone(),
                    entry.getBusiness().getName(),
                    entry.getBusiness().getPhone());
        } else {
            throw new IllegalStateException("Customer cannot be notified at this time");
        }
    }

    public void seatCustomer(UUID entryId) {
        WaitlistEntry entry = waitlistEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (entry.canBeSeated()) {
            entry.seatCustomer();
            waitlistEntryRepository.save(entry);

            // Update positions of remaining customers
            waitlistEntryRepository.updatePositionsAfterRemoval(
                    entry.getBusiness().getId(),
                    entry.getPosition());
        } else {
            throw new IllegalStateException("Customer cannot be seated at this time");
        }
    }

    public void cancelWaitlistEntry(UUID entryId) {
        WaitlistEntry entry = waitlistEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (entry.isActive()) {
            entry.cancel();
            waitlistEntryRepository.save(entry);

            // Update positions of remaining customers
            waitlistEntryRepository.updatePositionsAfterRemoval(
                    entry.getBusiness().getId(),
                    entry.getPosition());
        } else {
            throw new IllegalStateException("Waitlist entry is not active");
        }
    }
}


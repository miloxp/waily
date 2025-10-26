package com.waitlist.application.usecase;

import com.waitlist.application.dto.AddCustomerToWaitlistRequest;
import com.waitlist.application.dto.AddCustomerToWaitlistResponse;
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
public class AddCustomerToWaitlistUseCase {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final SmsService smsService;

    @Autowired
    public AddCustomerToWaitlistUseCase(WaitlistEntryRepository waitlistEntryRepository,
            BusinessRepository businessRepository,
            CustomerRepository customerRepository,
            SmsService smsService) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.businessRepository = businessRepository;
        this.customerRepository = customerRepository;
        this.smsService = smsService;
    }

    public AddCustomerToWaitlistResponse execute(AddCustomerToWaitlistRequest request, UUID businessId) {
        // Validate business exists and is active
        Business business = validateBusiness(businessId);

        // Validate customer exists
        Customer customer = validateCustomer(request.getCustomerId());

        // Check if customer is already on the waitlist
        validateCustomerNotOnWaitlist(businessId, request.getCustomerId());

        // Get next position in waitlist
        Integer nextPosition = getNextPosition(businessId);

        // Create waitlist entry
        WaitlistEntry entry = createWaitlistEntry(business, customer, request.getPartySize(), nextPosition);

        // Save waitlist entry
        WaitlistEntry savedEntry = waitlistEntryRepository.save(entry);

        // Send SMS notification
        boolean smsSent = sendSmsNotification(savedEntry);

        // Build and return response
        return buildResponse(savedEntry, smsSent);
    }

    private Business validateBusiness(UUID businessId) {
        return businessRepository.findById(businessId)
                .filter(Business::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("Business not found or inactive"));
    }

    private Customer validateCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    private void validateCustomerNotOnWaitlist(UUID businessId, UUID customerId) {
        if (waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId).isPresent()) {
            throw new IllegalStateException("Customer is already on the waitlist");
        }
    }

    private Integer getNextPosition(UUID businessId) {
        return waitlistEntryRepository.findMaxPositionForBusiness(businessId)
                .orElse(0) + 1;
    }

    private WaitlistEntry createWaitlistEntry(Business business, Customer customer, Integer partySize,
            Integer position) {
        WaitlistEntry entry = new WaitlistEntry(business, customer, partySize, position);

        // Calculate estimated wait time based on business average service time and
        // position
        entry.calculateEstimatedWaitTime(business.getAverageServiceTime(), position);

        return entry;
    }

    private boolean sendSmsNotification(WaitlistEntry entry) {
        try {
            return smsService.sendWaitlistNotification(
                    entry.getCustomer().getPhone(),
                    entry.getBusiness().getName(),
                    entry.getEstimatedWaitTime(),
                    entry.getPosition());
        } catch (Exception e) {
            // Log error but don't fail the operation
            // In a real application, you might want to retry or queue the SMS
            return false;
        }
    }

    private AddCustomerToWaitlistResponse buildResponse(WaitlistEntry entry, boolean smsSent) {
        return new AddCustomerToWaitlistResponse(
                entry.getId(),
                entry.getBusiness().getId(),
                entry.getCustomer().getId(),
                entry.getBusiness().getName(),
                entry.getCustomer().getName(),
                entry.getCustomer().getPhone(),
                entry.getPartySize(),
                entry.getPosition(),
                entry.getEstimatedWaitTime(),
                entry.getStatus(),
                entry.getCreatedAt(),
                smsSent);
    }
}


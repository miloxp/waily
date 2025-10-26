package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.WaitlistEntry;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.WaitlistEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/public")
@Tag(name = "Public", description = "Public endpoints (no authentication required)")
public class PublicController {

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @GetMapping("/waitlist/{businessId}")
    @Operation(summary = "Get public waitlist info", description = "Public endpoint showing estimated wait time for a business")
    public ResponseEntity<Map<String, Object>> getPublicWaitlistInfo(@PathVariable UUID businessId) {
        try {
            // Get business info
            Optional<Business> business = businessRepository.findById(businessId);
            if (business.isEmpty() || !business.get().getIsActive()) {
                return ResponseEntity.notFound().build();
            }

            Business businessEntity = business.get();

            // Get current waitlist stats
            List<WaitlistEntry> waitingEntries = waitlistEntryRepository.findWaitingEntries(businessId);
            long totalWaiting = waitingEntries.size();

            // Calculate average wait time
            Integer averageWaitTime = null;
            if (totalWaiting > 0) {
                int totalWaitTime = waitingEntries.stream()
                        .mapToInt(entry -> entry.getEstimatedWaitTime() != null ? entry.getEstimatedWaitTime() : 0)
                        .sum();
                averageWaitTime = totalWaitTime / (int) totalWaiting;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("businessName", businessEntity.getName());
            response.put("businessType", businessEntity.getType());
            response.put("totalWaiting", totalWaiting);
            response.put("averageWaitTime", averageWaitTime);
            response.put("averageServiceTime", businessEntity.getAverageServiceTime());
            response.put("capacity", businessEntity.getCapacity());
            response.put("isActive", businessEntity.getIsActive());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


package com.codebreaker.application.reservations.availability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    private static final Logger log =
            LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvailabilityService service;

    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @Valid @RequestBody CheckAvailabilityRequest request
    ) {
        log.info(
                "Checking room availability: roomId={}, startDate={}, endDate={}",
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        boolean available = service.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        AvailabilityStatus status = available
                ? AvailabilityStatus.AVAILABLE
                : AvailabilityStatus.RESERVED;

        String message = available
                ? "Room is available"
                : "Room is not available";

        return ResponseEntity.ok(
                new CheckAvailabilityResponse(message, status)
        );
    }
}

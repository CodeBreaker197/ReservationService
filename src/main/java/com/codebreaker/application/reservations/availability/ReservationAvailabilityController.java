package com.codebreaker.application.reservations.availability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);
    private ReservationAvailabilityService service;

    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @Valid CheckAvailabilityRequest request
    ) {
        log.info("Called method checkAvailability(), request={}", request);
        boolean isAvailable = service.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        var message = isAvailable ?
                "Room is available to reservation" :
                "Room is not available to reservation";
        var status = isAvailable ?
                AvailabilityStatus.AVAILABLE :
                AvailabilityStatus.RESERVED;

        return ResponseEntity.status(200)
                .body(new CheckAvailabilityResponse(message, status));
    }

}

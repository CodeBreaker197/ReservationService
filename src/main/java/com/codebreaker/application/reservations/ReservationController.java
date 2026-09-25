package com.codebreaker.application.reservations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log =
            LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable Long id
    ) {
        log.info("Get reservation: id={}", id);
        return ResponseEntity.ok(
                reservationService.getReservationById(id)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Reservation>> getAllReservationsByStatus(
            @PathVariable ReservationStatus status
    ) {
        log.info("Get reservations by status: status={}", status);
        return ResponseEntity.ok(
                reservationService.findAllReservationsByStatus(status)
        );
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer pageNumber
    ) {
        return ResponseEntity.ok(
                reservationService.searchAllByFilter(
                        new ReservationSearchFilter(
                                userId,
                                roomId,
                                pageSize,
                                pageNumber
                        )
                )
        );
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Reservation>> getAllReservationsByRoomId(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(
                reservationService.findAllReservationsByRoomId(roomId)
        );
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody Reservation reservationToCreate
    ) {
        Reservation createdReservation =
                reservationService.createReservation(reservationToCreate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReservation);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reservationService.approveReservation(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody Reservation reservationToUpdate
    ) {
        return ResponseEntity.ok(
                reservationService.updateReservation(id, reservationToUpdate)
        );
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id
    ) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}

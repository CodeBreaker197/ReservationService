package com.codebreaker.application.reservations;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.List;


@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long reservationId
    ) {
        log.info("Called getReservationById({})", reservationId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(reservationId));
    }

    @GetMapping("/status/{reservationStatus}")
    public ResponseEntity<List<Reservation>> getAllReservationsByStatus(
            @PathVariable ReservationStatus reservationStatus
    ) {
        log.info("Called getAllReservationsByStatus() with status: {}", reservationStatus);
        return ResponseEntity
                .ok(reservationService.findAllReservationsByStatus(reservationStatus));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("Called getAllReservations()");
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Reservation>> getAllReservationsByRoomId(
            @PathVariable Long roomId
    ) {
        log.info("Get getAllReservationsByRoomId() id={}", roomId);
        return ResponseEntity
                .ok(reservationService.findAllReservationsByRoomId(roomId));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservationToCreate
    ) {
        log.info("Called createReservation()");

        Reservation createdReservation =
                reservationService.createReservation(reservationToCreate);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdReservation);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable Long id
    ) {
        log.info("Called approveReservation, id={}", id);

        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok().body(reservation);
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Void> setStatus( // Function for administrators
            @PathVariable Long id,
            @PathVariable ReservationStatus status
    ) {
        log.info("Called setStatus(), id={}", id);
        reservationService.setStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @RequestBody @Valid Reservation reservationToUpdate
    ) {
        log.info("Called updateReservation id={}, reservationToUpdate={}",
                id, reservationToUpdate);

        var updated = reservationService.
                updateReservation(id, reservationToUpdate);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        log.info("Called deleteReservation id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok()
                .build();
    }
}

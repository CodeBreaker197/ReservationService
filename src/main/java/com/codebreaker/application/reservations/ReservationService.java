package com.codebreaker.application.reservations;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.*;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationMapper mapper;

    public ReservationService(
            ReservationRepository repository,
            ReservationMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Reservation getReservationById(
            Long id
    ) {

        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id
                ));

        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllReservationsByStatus(
            ReservationStatus reservationStatus
    ) {
        List<ReservationEntity> allEntities = repository.findAllByStatusIs(reservationStatus);

        return allEntities.stream()
                .map(mapper::toDomain).toList();
    }

    public List<Reservation> findAllReservations() {

        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(mapper::toDomain).toList();
    }

    public void setStatus( // Function for administrators
            Long id,
            ReservationStatus status
    ) {
        List<ReservationStatus> statusList = List.of(
                ReservationStatus.APPROVED,
                ReservationStatus.PENDING,
                ReservationStatus.CANCELLED
        );

        if(!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found reservation by id=" + id);
        }

        repository.setStatus(id, status);
        log.info("Successfully set status={}", status);
    }

    public Reservation createReservation(
            Reservation reservationToCreate
    ) {
        if(reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if(!reservationToCreate.startDate().isBefore(reservationToCreate.endDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        if(isReservationConflict(entityToSave)) {
            throw new IllegalStateException("This room is already booked with the selected dates");
        }

        var savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public List<Reservation> findAllReservationsByRoomId(
            Long roomId
    ) {
        List<ReservationEntity> allEntities = repository.findAllReservationsByRoomId(roomId);

        if(allEntities.isEmpty()) {
            throw new EntityNotFoundException("Not found reservations by roomId = " + roomId);
        }

        return allEntities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Reservation updateReservation(
            Long id,
            Reservation reservationToUpdate
    ) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot modify reservation: status=" + reservationEntity.getStatus());
        }

        if(reservationToUpdate.startDate().isBefore(reservationToUpdate.endDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        var reservationToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
    }

    public void cancelReservation(Long id) {
        var reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        ReservationStatus status = reservation.getStatus();

        if(status.equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        } else if(status.equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel the reservation. Reservation was already cancelled");
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation: status=" + reservationEntity.getStatus());
        }

        var isConflict = isReservationConflict(reservationEntity);

        if(isConflict) {
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

    private boolean isReservationConflict(
            ReservationEntity reservation
    ) {

        return repository.hasConflict(
                reservation.getRoomId(),
                ReservationStatus.APPROVED,
                reservation.getId(),
                reservation.getStartDate(),
                reservation.getEndDate()
        );
    }
}

package com.codebreaker.application.reservations;

import com.codebreaker.application.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationAvailabilityService availabilityService;
    private final ReservationRepository repository;
    private final ReservationMapper mapper;

    public ReservationService(
            ReservationAvailabilityService availabilityService,
            ReservationRepository repository,
            ReservationMapper mapper
    ) {
        this.availabilityService = availabilityService;
        this.repository = repository;
        this.mapper = mapper;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Reservation not found: id=" + id
                ));

        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllReservationsByStatus(ReservationStatus status) {
        return repository.findAllByStatusIs(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public List<Reservation> searchAllByFilter(ReservationSearchFilter filter) {
        int pageSize = filter.pageSize() == null ? 10 : filter.pageSize();
        int pageNumber = filter.pageNumber() == null ? 0 : filter.pageNumber();

        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be greater than or equal to 0");
        }

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        return repository.searchAllByFilter(
                        filter.userId(),
                        filter.roomId(),
                        pageable
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        validateDateRange(
                reservationToCreate.startDate(),
                reservationToCreate.endDate()
        );

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException(
                    "Status must not be provided when creating a reservation"
            );
        }

        ReservationEntity entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        ReservationEntity savedEntity = repository.save(entityToSave);
        log.info(
                "Reservation created: id={}, roomId={}, userId={}",
                savedEntity.getId(),
                savedEntity.getRoomId(),
                savedEntity.getUserId()
        );

        return mapper.toDomain(savedEntity);
    }

    public List<Reservation> findAllReservationsByRoomId(Long roomId) {
        return repository.findAllByRoomId(roomId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        ReservationEntity existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Reservation not found: id=" + id
                ));

        if (existing.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending reservations can be modified"
            );
        }

        validateDateRange(
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate()
        );

        if (reservationToUpdate.status() != null) {
            throw new IllegalArgumentException(
                    "Status must not be provided when updating a reservation"
            );
        }

        ReservationEntity updated = new ReservationEntity(
                existing.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        ReservationEntity saved = repository.save(updated);
        log.info("Reservation updated: id={}", id);

        return mapper.toDomain(saved);
    }

    public void cancelReservation(Long id) {
        ReservationEntity reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Reservation not found: id=" + id
                ));

        if (reservation.getStatus() == ReservationStatus.APPROVED) {
            throw new IllegalStateException(
                    "Approved reservations cannot be cancelled"
            );
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Reservation is already cancelled"
            );
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Reservation cancelled: id={}", id);
    }

    public Reservation approveReservation(Long id) {
        ReservationEntity reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Reservation not found: id=" + id
                ));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending reservations can be approved"
            );
        }

        boolean available = availabilityService.isReservationAvailable(
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate()
        );

        if (!available) {
            throw new IllegalStateException(
                    "Cannot approve reservation because the room is already reserved for this period"
            );
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        ReservationEntity saved = repository.save(reservation);

        log.info("Reservation approved: id={}", id);
        return mapper.toDomain(saved);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
    }
}

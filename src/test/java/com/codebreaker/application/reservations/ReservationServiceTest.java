package com.codebreaker.application.reservations;

import com.codebreaker.application.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationAvailabilityService availabilityService;

    @Mock
    private ReservationRepository repository;

    @Mock
    private ReservationMapper mapper;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(
                availabilityService,
                repository,
                mapper
        );
    }

    @Test
    void createReservationShouldDefaultStatusToPending() {
        Reservation request = reservation(
                null,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                null
        );

        ReservationEntity saved = new ReservationEntity(
                1L,
                request.userId(),
                request.roomId(),
                request.startDate(),
                request.endDate(),
                ReservationStatus.PENDING
        );

        Reservation expected = reservation(
                1L,
                request.startDate(),
                request.endDate(),
                ReservationStatus.PENDING
        );

        when(repository.save(any(ReservationEntity.class))).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(expected);

        Reservation result = service.createReservation(request);

        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<ReservationEntity> captor =
                ArgumentCaptor.forClass(ReservationEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void updateReservationShouldRejectInvalidDateRange() {
        ReservationEntity existing = new ReservationEntity(
                10L,
                1L,
                2L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                ReservationStatus.PENDING
        );

        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        Reservation request = reservation(
                null,
                LocalDate.of(2026, 10, 15),
                LocalDate.of(2026, 10, 12),
                null
        );

        assertThatThrownBy(() -> service.updateReservation(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must be before endDate");

        verify(repository, never()).save(any());
    }

    @Test
    void approveReservationShouldRejectConflictingRoom() {
        ReservationEntity existing = new ReservationEntity(
                10L,
                1L,
                2L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                ReservationStatus.PENDING
        );

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(availabilityService.isReservationAvailable(
                2L,
                existing.getStartDate(),
                existing.getEndDate()
        )).thenReturn(false);

        assertThatThrownBy(() -> service.approveReservation(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already reserved");

        verify(repository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void getReservationShouldThrowWhenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReservationById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Reservation not found: id=999");
    }

    private Reservation reservation(
            Long id,
            LocalDate startDate,
            LocalDate endDate,
            ReservationStatus status
    ) {
        return new Reservation(
                id,
                1L,
                2L,
                startDate,
                endDate,
                status
        );
    }
}

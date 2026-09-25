package com.codebreaker.application.reservations.availability;

import com.codebreaker.application.reservations.ReservationRepository;
import com.codebreaker.application.reservations.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationAvailabilityServiceTest {

    @Mock
    private ReservationRepository repository;

    private ReservationAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new ReservationAvailabilityService(repository);
    }

    @Test
    void shouldReturnAvailableWhenThereAreNoConflicts() {
        when(repository.findConflictIds(
                5L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                ReservationStatus.APPROVED
        )).thenReturn(List.of());

        boolean result = service.isReservationAvailable(
                5L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnUnavailableWhenConflictExists() {
        when(repository.findConflictIds(
                5L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                ReservationStatus.APPROVED
        )).thenReturn(List.of(42L));

        boolean result = service.isReservationAvailable(
                5L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectReversedDateRange() {
        assertThatThrownBy(() -> service.isReservationAvailable(
                5L,
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 10, 10)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must be before endDate");

        verifyNoInteractions(repository);
    }
}

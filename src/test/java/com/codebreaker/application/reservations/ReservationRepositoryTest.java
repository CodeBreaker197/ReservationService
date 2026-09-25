package com.codebreaker.application.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository repository;

    @Test
    void shouldFindApprovedReservationThatOverlapsRequestedPeriod() {
        repository.save(new ReservationEntity(
                null,
                1L,
                10L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 15),
                ReservationStatus.APPROVED
        ));

        var conflictIds = repository.findConflictIds(
                10L,
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 10, 18),
                ReservationStatus.APPROVED
        );

        assertThat(conflictIds).hasSize(1);
    }

    @Test
    void shouldIgnoreCancelledReservations() {
        repository.save(new ReservationEntity(
                null,
                1L,
                10L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 15),
                ReservationStatus.CANCELLED
        ));

        var conflictIds = repository.findConflictIds(
                10L,
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 10, 18),
                ReservationStatus.APPROVED
        );

        assertThat(conflictIds).isEmpty();
    }

    @Test
    void shouldAllowBackToBackReservations() {
        repository.save(new ReservationEntity(
                null,
                1L,
                10L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 15),
                ReservationStatus.APPROVED
        ));

        var conflictIds = repository.findConflictIds(
                10L,
                LocalDate.of(2026, 10, 15),
                LocalDate.of(2026, 10, 20),
                ReservationStatus.APPROVED
        );

        assertThat(conflictIds).isEmpty();
    }
}

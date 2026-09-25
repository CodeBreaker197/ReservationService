package com.codebreaker.application.reservations;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record Reservation(
        @Null
        Long id,

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long roomId,

        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        @NotNull
        @FutureOrPresent
        LocalDate endDate,

        ReservationStatus status
) {
}

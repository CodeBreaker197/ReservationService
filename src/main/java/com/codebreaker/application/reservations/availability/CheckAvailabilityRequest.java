package com.codebreaker.application.reservations.availability;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CheckAvailabilityRequest(
        @NotNull
        @Positive
        Long roomId,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}

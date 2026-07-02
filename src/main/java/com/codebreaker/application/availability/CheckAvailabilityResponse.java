package com.codebreaker.application.availability;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {}

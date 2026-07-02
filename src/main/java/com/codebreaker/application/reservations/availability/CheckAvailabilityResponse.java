package com.codebreaker.application.reservations.availability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {}

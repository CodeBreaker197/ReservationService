package com.codebreaker.application.reservations;

public record ReservationSearchFilter(
        Long userId,
        Long roomId,
        Integer pageSize,
        Integer pageNumber
) {}

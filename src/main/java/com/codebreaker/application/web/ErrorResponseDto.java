package com.codebreaker.application.web;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String message,
        String details,
        LocalDateTime timestamp
) {
}

package com.codebreaker.application.web;

import java.time.LocalDateTime;

public record ErrorResponceDto(
        String message,
        String detailedMessage, // errorMessage
        LocalDateTime errorTime
) {}

package com.badsector.anakronik.dto;

import java.time.Instant;

public record DocumentDto(
        Long id,
        String docName,
        Long historicalFigureId,
        Instant createdAt
) {}

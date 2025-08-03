package com.badsector.anakronik.dto;

import java.time.Instant;

public record HistoricalFigureDto(
        Long id,
        String name,
        String birthDate,
        String deathDate,
        String bio,
        String region,
        Instant createdAt,
        String imageUrl,
        String createdByUsername
){}
package com.badsector.anakronik.dto;

import java.time.Instant;

public record HistoricalFigureDto (
        Long id,
        String name,
        String birthDate,
        String deathDate,
        String bio,
        Instant createdAt,
        String region
){}


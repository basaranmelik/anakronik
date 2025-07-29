package com.badsector.anakronik.dto;

import com.badsector.anakronik.model.WorldRegion;

import java.time.Instant;

public record HistoricalFigureDto (
        Long id,
        String name,
        String birthDate,
        String deathDate,
        String bio,
        Instant createdAt,
        WorldRegion region
){}


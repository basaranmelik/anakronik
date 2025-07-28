package com.badsector.anakronik.dto;

import java.time.LocalDate;
import java.time.Instant;

public record HistoricalFigureDto (
    Long id,
    String name,
    LocalDate birthDate,
    LocalDate deathDate,
    String bio,
    Instant createdAt,
    String region
){}


package com.badsector.anakronik.dto;

import java.time.LocalDate;

public record CreateHistoricalFigureRequest(String name, LocalDate birthDate, LocalDate deathDate, String bio) {}

package com.badsector.anakronik.controller.dto;

import jakarta.validation.constraints.NotBlank; // Validation eklemek iyi bir pratiktir

public record CreateHistoricalFigureRequest(
        @NotBlank(message = "Name cannot be blank")
        String name,

        String bio
) {}
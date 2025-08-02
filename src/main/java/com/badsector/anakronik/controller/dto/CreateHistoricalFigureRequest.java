package com.badsector.anakronik.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHistoricalFigureRequest(
        @NotBlank(message = "Name cannot be blank")
        String name
) {}
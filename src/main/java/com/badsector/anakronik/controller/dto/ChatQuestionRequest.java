package com.badsector.anakronik.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatQuestionRequest(
        @NotBlank(message = "Question cannot be blank")
        String question
) {}
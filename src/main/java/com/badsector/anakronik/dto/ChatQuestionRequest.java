package com.badsector.anakronik.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Kullanıcının sohbet için gönderdiği soruyu içeren DTO.
 */
public record ChatQuestionRequest(
        @NotBlank(message = "Question cannot be blank")
        String question
) {}
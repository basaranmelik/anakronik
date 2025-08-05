package com.badsector.anakronik.controller.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Full name cannot be blank")
        @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
        String fullName
) {}
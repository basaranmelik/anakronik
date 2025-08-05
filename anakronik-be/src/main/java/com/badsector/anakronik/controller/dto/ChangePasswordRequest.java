package com.badsector.anakronik.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Eski şifre boş olamaz.")
        String oldPassword,

        @NotBlank(message = "Yeni şifre boş olamaz.")
        @Size(min = 8, message = "Yeni şifre en az 8 karakter olmalıdır.")
        String newPassword
) {}
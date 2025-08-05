package com.badsector.anakronik.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token boş olamaz")
        String token,

        @NotBlank(message = "Yeni şifre boş olamaz")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
        String newPassword
) {}
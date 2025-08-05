package com.badsector.anakronik.controller.dto;


import jakarta.validation.constraints.Size;

public record RegisterRequest (String fullName, String email,
                               @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
                               String password){}
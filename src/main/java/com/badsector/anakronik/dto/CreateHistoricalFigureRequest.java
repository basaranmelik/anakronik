package com.badsector.anakronik.dto;

import jakarta.validation.constraints.NotBlank; // Validation eklemek iyi bir pratiktir

/**
 * Yeni bir tarihi şahsiyet oluşturmak için kullanıcıdan alınan temel bilgileri içerir.
 * Doğum/ölüm tarihi gibi detaylar RAG süreciyle otomatik olarak doldurulacaktır.
 */
public record CreateHistoricalFigureRequest(
        @NotBlank(message = "Name cannot be blank")
        String name,

        String bio
) {}
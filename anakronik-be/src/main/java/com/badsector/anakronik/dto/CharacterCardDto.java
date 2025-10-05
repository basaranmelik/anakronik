package com.badsector.anakronik.dto;

public record CharacterCardDto(
        Long id,
        String name,
        String bio,
        String birthDate,
        String deathDate,
        String imageUrl) {
}
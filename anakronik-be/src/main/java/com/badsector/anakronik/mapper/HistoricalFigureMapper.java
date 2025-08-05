package com.badsector.anakronik.mapper;

import com.badsector.anakronik.dto.CharacterCardDto;
import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.model.HistoricalFigure;
import org.springframework.stereotype.Component;

@Component
public class HistoricalFigureMapper {

    public HistoricalFigureDto toDto(HistoricalFigure figure) {
        if (figure == null) {
            return null;
        }
        String username = (figure.getCreatedBy() != null) ? figure.getCreatedBy().getUsername() : null;

        return new HistoricalFigureDto(
                figure.getId(),
                figure.getName(),
                figure.getBirthDate(),
                figure.getDeathDate(),
                figure.getBio(),
                figure.getRegion(),
                figure.getCreatedAt(),
                figure.getImageUrl(),
                username);
    }

    public CharacterCardDto toCharacterCardDto(HistoricalFigure figure) {
        if (figure == null) {
            return null;
        }

        return new CharacterCardDto(
                figure.getId(),
                figure.getName(),
                figure.getBio(),
                figure.getBirthDate(),
                figure.getDeathDate(),
                figure.getImageUrl());
    }
}
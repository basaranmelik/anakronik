// com.badsector.anakronik.mapper.HistoricalFigureMapper.java
package com.badsector.anakronik.mapper;

import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.model.HistoricalFigure;
import org.springframework.stereotype.Component;

@Component
public class HistoricalFigureMapper {

    public HistoricalFigureDto toDto(HistoricalFigure figure) {
        if (figure == null) {
            return null;
        }
        return new HistoricalFigureDto(
                figure.getId(),
                figure.getName(),
                figure.getBirthDate(),
                figure.getDeathDate(),
                figure.getBio(),
                figure.getCreatedAt(),
                figure.getRegion()
        );
    }
}
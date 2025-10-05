package com.badsector.anakronik.mapper;

import com.badsector.anakronik.dto.DocumentDto;
import com.badsector.anakronik.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public static DocumentDto toDto(Document document) {
        if (document == null) {
            return null;
        }
        return new DocumentDto(
                document.getId(),
                document.getDocName(),
                document.getHistoricalFigure().getId(),
                document.getCreatedAt()
        );
    }
}
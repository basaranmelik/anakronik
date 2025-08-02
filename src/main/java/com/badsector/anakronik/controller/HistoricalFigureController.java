package com.badsector.anakronik.controller;

import com.badsector.anakronik.controller.dto.CreateHistoricalFigureRequest;
import com.badsector.anakronik.dto.DocumentDto;
import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.service.HistoricalFigureService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/historical-figures")
public class HistoricalFigureController {

    private final HistoricalFigureService historicalFigureService;

    public HistoricalFigureController(HistoricalFigureService historicalFigureService) {
        this.historicalFigureService = historicalFigureService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<String> createFigure(
            @Valid @RequestPart("figureData") CreateHistoricalFigureRequest figureRequest,
            @RequestPart("image") MultipartFile imageFile,
            @RequestPart("file") MultipartFile docFile,
            Authentication authentication
    ) throws IOException {
        String currentUsername = authentication.getName();
        historicalFigureService.createFigureAndFirstDocument(figureRequest, docFile, imageFile, currentUsername);

        String responseMessage = String.format(
                "Figür '%s' başarıyla oluşturuldu. (Oluşturan: %s)",
                figureRequest.name(),
                currentUsername
        );

        return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
    }

    @PostMapping("/{figureId}/add-document")
    public ResponseEntity<DocumentDto> addAdditionalDocumentToFigure(
            @PathVariable Long figureId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        var savedDocument = historicalFigureService.addDocumentToOwnedFigure(figureId, file, authentication.getName());
        return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<HistoricalFigureDto>> getFiguresForCurrentUser(Pageable pageable, Authentication authentication) {
        Page<HistoricalFigureDto> figuresPage = historicalFigureService.findVisibleFiguresForUser(
                authentication.getName(),
                pageable
        );
        return ResponseEntity.ok(figuresPage);
    }

    @GetMapping("/{figureId}")
    public ResponseEntity<HistoricalFigureDto> getFigureById(@PathVariable Long figureId, Authentication authentication) {
        HistoricalFigureDto figureDto = historicalFigureService.getFigureByIdForUser(figureId, authentication.getName());
        return ResponseEntity.ok(figureDto);
    }

    @DeleteMapping("/{figureId}")
    public ResponseEntity<Void> deleteHistoricalFigure(@PathVariable Long figureId, Authentication authentication) {
        historicalFigureService.deleteFigureForUser(figureId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
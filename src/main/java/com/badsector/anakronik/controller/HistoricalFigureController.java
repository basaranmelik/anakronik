package com.badsector.anakronik.controller;

import com.badsector.anakronik.dto.AddDocumentRequest;
import com.badsector.anakronik.dto.CreateHistoricalFigureRequest;
import com.badsector.anakronik.dto.DocumentDto;
import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.mapper.DocumentMapper;
import com.badsector.anakronik.service.HistoricalFigureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // YENİ IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/historical-figures")
public class HistoricalFigureController {

    private final HistoricalFigureService historicalFigureService;
    private final DocumentMapper documentMapper; // DocumentDto döndürmek için hala gerekli

    public HistoricalFigureController(HistoricalFigureService historicalFigureService, DocumentMapper documentMapper) {
        this.historicalFigureService = historicalFigureService;
        this.documentMapper = documentMapper;
    }

    //TODO Burası girdi olarak düzenlenecek multipart form için file ve json olarak iki ayrı veri geldiğinde http header ayarlayamıyom
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<HistoricalFigureDto> createFigureWithDocument(
            @RequestPart("figureData") String figureRequestJson,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateHistoricalFigureRequest figureRequest = objectMapper.readValue(figureRequestJson, CreateHistoricalFigureRequest.class);
        HistoricalFigureDto newFigureDto = historicalFigureService.createFigureAndFirstDocument(
                figureRequest,
                file,
                authentication.getName()
        );
        return new ResponseEntity<>(newFigureDto, HttpStatus.CREATED);
    }
    @PostMapping("/{figureId}/documents")
    public ResponseEntity<DocumentDto> addAdditionalDocumentToFigure(
            @PathVariable Long figureId,
            @RequestParam("docName") String docName,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        AddDocumentRequest request = new AddDocumentRequest(docName, file);
        DocumentDto newDocumentDto = documentMapper.toDto(
                historicalFigureService.addDocumentToOwnedFigure(figureId, request, authentication.getName())
        );
        return new ResponseEntity<>(newDocumentDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<HistoricalFigureDto>> getFiguresForCurrentUser(Pageable pageable, Authentication authentication) {
        Page<HistoricalFigureDto> myFiguresPage = historicalFigureService.findFiguresByUser(authentication.getName(), pageable);
        return ResponseEntity.ok(myFiguresPage);
    }

    @GetMapping("/{figureId}")
    public ResponseEntity<HistoricalFigureDto> getFigureById(@PathVariable Long figureId, Authentication authentication) {
        HistoricalFigureDto figureDto = historicalFigureService.getFigureByIdForUser(figureId, authentication.getName());
        return ResponseEntity.ok(figureDto);
    }

    @PutMapping("/{figureId}")
    public ResponseEntity<HistoricalFigureDto> updateHistoricalFigure(@PathVariable Long figureId, @Valid @RequestBody CreateHistoricalFigureRequest request, Authentication authentication) {
        HistoricalFigureDto updatedFigure = historicalFigureService.updateFigureForUser(figureId, request, authentication.getName());
        return ResponseEntity.ok(updatedFigure);
    }

    @DeleteMapping("/{figureId}")
    public ResponseEntity<Void> deleteHistoricalFigure(@PathVariable Long figureId, Authentication authentication) {
        historicalFigureService.deleteFigureForUser(figureId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
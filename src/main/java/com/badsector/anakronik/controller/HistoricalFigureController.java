package com.badsector.anakronik.controller;

import com.badsector.anakronik.dto.*;
import com.badsector.anakronik.mapper.DocumentMapper;
import com.badsector.anakronik.service.HistoricalFigureService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/historical-figures")
public class HistoricalFigureController {

    private final HistoricalFigureService historicalFigureService;
    private final DocumentMapper documentMapper;

    public HistoricalFigureController(HistoricalFigureService historicalFigureService, DocumentMapper documentMapper) {
        this.historicalFigureService = historicalFigureService;
        this.documentMapper = documentMapper;
    }

    /**
     * Mevcut kullanıcı için yeni bir tarihi şahsiyet oluşturur.
     */
    @PostMapping
    public ResponseEntity<HistoricalFigureDto> createHistoricalFigure(
            @Valid @RequestBody CreateHistoricalFigureRequest request,
            Authentication authentication
    ) {
        // Servis zaten DTO döndüğü için tekrar maplemeye gerek yok.
        HistoricalFigureDto newFigureDto = historicalFigureService.createHistoricalFigure(request, authentication.getName());
        return new ResponseEntity<>(newFigureDto, HttpStatus.CREATED);
    }

    /**
     * Mevcut kullanıcıya ait tarihi şahsiyetleri sayfalı olarak listeler.
     */
    @GetMapping
    public ResponseEntity<Page<HistoricalFigureDto>> getFiguresForCurrentUser(
            Pageable pageable,
            Authentication authentication
    ) {
        // Servisten CustomPageDto'yu doğrudan alıyoruz.
        Page<HistoricalFigureDto> myFiguresPage = historicalFigureService.findFiguresByUser(authentication.getName(), pageable);
        return ResponseEntity.ok(myFiguresPage);
    }

    /**
     * Mevcut kullanıcıya ait tek bir tarihi şahsiyeti ID'sine göre getirir.
     */
    @GetMapping("/{figureId}")
    public ResponseEntity<HistoricalFigureDto> getFigureById(
            @PathVariable Long figureId,
            Authentication authentication
    ) {
        HistoricalFigureDto figureDto = historicalFigureService.getFigureByIdForUser(figureId, authentication.getName());
        return ResponseEntity.ok(figureDto);
    }

    /**
     * Mevcut kullanıcıya ait bir tarihi şahsiyeti günceller.
     */
    @PutMapping("/{figureId}")
    public ResponseEntity<HistoricalFigureDto> updateHistoricalFigure(
            @PathVariable Long figureId,
            @Valid @RequestBody CreateHistoricalFigureRequest request,
            Authentication authentication
    ) {
        HistoricalFigureDto updatedFigure = historicalFigureService.updateFigureForUser(figureId, request, authentication.getName());
        return ResponseEntity.ok(updatedFigure);
    }

    /**
     * Mevcut kullanıcıya ait bir tarihi şahsiyeti siler.
     */
    @DeleteMapping("/{figureId}")
    public ResponseEntity<Void> deleteHistoricalFigure(
            @PathVariable Long figureId,
            Authentication authentication
    ) {
        historicalFigureService.deleteFigureForUser(figureId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // --- Belge (Document) Endpoint'leri ---

    /**
     * Mevcut kullanıcının sahip olduğu bir tarihi şahsiyete belge ekler.
     */
    @PostMapping("/{figureId}/documents")
    public ResponseEntity<DocumentDto> addDocumentToFigure(
            @PathVariable Long figureId,
            @RequestParam("docName") String docName,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws IOException { // Hata yönetimi GlobalExceptionHandler'a devredildi
        AddDocumentRequest request = new AddDocumentRequest(docName, file);
        // Güvenli servis metodu çağrılıyor ve sonuç DTO'ya çevriliyor.
        DocumentDto newDocumentDto = documentMapper.toDto(
                historicalFigureService.addDocumentToOwnedFigure(figureId, request, authentication.getName())
        );
        return new ResponseEntity<>(newDocumentDto, HttpStatus.CREATED);
    }
}
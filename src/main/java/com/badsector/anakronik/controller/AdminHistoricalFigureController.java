package com.badsector.anakronik.controller;

import com.badsector.anakronik.controller.dto.CreateHistoricalFigureRequest;
import com.badsector.anakronik.dto.DocumentDto; // Gerekirse oluşturun
import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.service.HistoricalFigureService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/historical-figures")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHistoricalFigureController {

    private final HistoricalFigureService historicalFigureService;

    public AdminHistoricalFigureController(HistoricalFigureService historicalFigureService) {
        this.historicalFigureService = historicalFigureService;
    }

    /**
     * [ADMIN] Tüm tarihi karakterleri listeler.
     */
    @GetMapping
    public ResponseEntity<Page<HistoricalFigureDto>> getAllFigures(Pageable pageable) {
        Page<HistoricalFigureDto> figures = historicalFigureService.getAllFiguresAsAdmin(pageable);
        return ResponseEntity.ok(figures);
    }


    /**
     * [ADMIN] ID ile belirtilen bir karaktere yeni bir doküman ekler.
     */
    @PostMapping("/{figureId}/documents")
    public ResponseEntity<DocumentDto> addDocumentToAnyFigure(@PathVariable Long figureId, @RequestPart("file") MultipartFile file) throws IOException {
        DocumentDto newDocument = historicalFigureService.addDocumentAsAdmin(figureId, file);
        return new ResponseEntity<>(newDocument, HttpStatus.CREATED);
    }

    /**
     * [ADMIN] ID ile belirtilen bir karakteri ve ilişkili tüm verileri siler.
     */
    @DeleteMapping("/{figureId}")
    public ResponseEntity<Void> deleteAnyFigure(@PathVariable Long figureId) {
        historicalFigureService.deleteFigureAsAdmin(figureId);
        return ResponseEntity.noContent().build();
    }
}
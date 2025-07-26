package com.badsector.anakronik.service;

import com.badsector.anakronik.dto.AddDocumentRequest;
import com.badsector.anakronik.dto.CreateHistoricalFigureRequest;
import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.mapper.DocumentMapper;
import com.badsector.anakronik.mapper.HistoricalFigureMapper;
import com.badsector.anakronik.model.Document;
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.repository.DocumentRepository;
import com.badsector.anakronik.repository.HistoricalFigureRepository;
import com.badsector.anakronik.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricalFigureService {

    private final HistoricalFigureRepository historicalFigureRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final RagProcessingService ragProcessingService;
    private final HistoricalFigureMapper historicalFigureMapper;

    public HistoricalFigureService(HistoricalFigureRepository historicalFigureRepository,
                                   DocumentRepository documentRepository,
                                   UserRepository userRepository,
                                   FileStorageService fileStorageService,
                                   RagProcessingService ragProcessingService,
                                   HistoricalFigureMapper historicalFigureMapper) {
        this.historicalFigureRepository = historicalFigureRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.ragProcessingService = ragProcessingService;
        this.historicalFigureMapper = historicalFigureMapper;
    }

    // ... createHistoricalFigure, addDocumentToOwnedFigure, getFigureByIdForUser, updateFigureForUser, deleteFigureForUser metotları aynı kalır ...
    // Sadece findFiguresByUser metodu güncellendi:

    @Transactional
    public HistoricalFigureDto createHistoricalFigure(CreateHistoricalFigureRequest request, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);

        HistoricalFigure figure = new HistoricalFigure();
        figure.setName(request.name());
        figure.setBirthDate(request.birthDate());
        figure.setDeathDate(request.deathDate());
        figure.setBio(request.bio());
        figure.setCreatedBy(currentUser);
        figure.setCreatedAt(Instant.now());

        HistoricalFigure savedFigure = historicalFigureRepository.save(figure);
        return historicalFigureMapper.toDto(savedFigure);
    }

    @Transactional
    public Document addDocumentToOwnedFigure(Long figureId, AddDocumentRequest request, String currentUserEmail) throws IOException {
        HistoricalFigure figure = findFigureByIdAndUser(figureId, currentUserEmail);
        Path filePath = fileStorageService.storeFile(request.file());
        Document document = new Document();
        document.setHistoricalFigure(figure);
        document.setDocName(request.docName());
        document.setFilePath(filePath.toString());
        document.setCreatedAt(Instant.now());
        Document savedDocument = documentRepository.save(document);
        ragProcessingService.sendDocumentToRag(filePath, savedDocument.getDocName(), figure.getId(), figure.getName(), figure.getCreatedBy().getId());
        return savedDocument;
    }

    @Transactional(readOnly = true)
    public Page<HistoricalFigureDto> findFiguresByUser(String currentUserEmail, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);
        Page<HistoricalFigure> figuresPage = historicalFigureRepository.findByCreatedBy(currentUser, pageable);

        // Entity sayfasını DTO sayfasına dönüştürmek için .map() kullanılır.
        return figuresPage.map(historicalFigureMapper::toDto);
    }

    @Transactional(readOnly = true)
    public HistoricalFigureDto getFigureByIdForUser(Long figureId, String currentUserEmail) {
        HistoricalFigure figure = findFigureByIdAndUser(figureId, currentUserEmail);
        return historicalFigureMapper.toDto(figure);
    }

    @Transactional
    public HistoricalFigureDto updateFigureForUser(Long figureId, CreateHistoricalFigureRequest request, String currentUserEmail) {
        HistoricalFigure figureToUpdate = findFigureByIdAndUser(figureId, currentUserEmail);
        figureToUpdate.setName(request.name());
        figureToUpdate.setBio(request.bio());
        figureToUpdate.setBirthDate(request.birthDate());
        figureToUpdate.setDeathDate(request.deathDate());
        HistoricalFigure updatedFigure = historicalFigureRepository.save(figureToUpdate);
        return historicalFigureMapper.toDto(updatedFigure);
    }

    @Transactional
    public void deleteFigureForUser(Long figureId, String currentUserEmail) {
        HistoricalFigure figureToDelete = findFigureByIdAndUser(figureId, currentUserEmail);
        documentRepository.deleteByHistoricalFigure(figureToDelete);
        historicalFigureRepository.delete(figureToDelete);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private HistoricalFigure findFigureByIdAndUser(Long figureId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return historicalFigureRepository.findByIdAndCreatedBy(figureId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));
    }
}
package com.badsector.anakronik.service;

import com.badsector.anakronik.controller.dto.CreateHistoricalFigureRequest;
import com.badsector.anakronik.dto.CharacterCardDto;
import com.badsector.anakronik.dto.DocumentDto;
import com.badsector.anakronik.dto.HistoricalFigureDto;
import com.badsector.anakronik.exception.RagServiceException; // Gateway'den gelen custom exception
import com.badsector.anakronik.exception.RagValidationException;
import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
import com.badsector.anakronik.gateway.dto.RagUploadResponse;
import com.badsector.anakronik.mapper.HistoricalFigureMapper;
import com.badsector.anakronik.model.Document;
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.UserRole;
import com.badsector.anakronik.repository.DocumentRepository;
import com.badsector.anakronik.repository.HistoricalFigureRepository;
import com.badsector.anakronik.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

@Service
public class HistoricalFigureService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalFigureService.class);

    private final HistoricalFigureRepository historicalFigureRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final RagServiceGatewayImpl ragServiceGateway;
    private final HistoricalFigureMapper historicalFigureMapper;

    public HistoricalFigureService(HistoricalFigureRepository historicalFigureRepository,
            DocumentRepository documentRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            RagServiceGatewayImpl ragServiceGateway,
            HistoricalFigureMapper historicalFigureMapper) {
        this.historicalFigureRepository = historicalFigureRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.ragServiceGateway = ragServiceGateway;
        this.historicalFigureMapper = historicalFigureMapper;
    }

    // --- KULLANICI İŞLEMLERİ ---

    /**
     * Yeni bir karakter oluşturur, ilk dokümanını RAG servisi ile işler ve her şeyi
     * tek bir transaction'da kaydeder.
     * RAG işlemi başarısız olursa, hiçbir kayıt veritabanına eklenmez.
     */
    @Transactional
    public HistoricalFigureDto createFigureAndFirstDocument(CreateHistoricalFigureRequest figureRequest,
            MultipartFile docFile, MultipartFile imageFile, String currentUserEmail) throws IOException {
        User currentUser = findUserByEmail(currentUserEmail);
        if (historicalFigureRepository.existsByNameAndCreatedBy(figureRequest.name(), currentUser)) {
            throw new IllegalStateException("Bu isimde bir karakter zaten mevcut.");
        }

        String imageUrl = fileStorageService.storeImage(imageFile);
        Path tempDocPath = fileStorageService.storeFileAsTemp(docFile);

        HistoricalFigure figure = new HistoricalFigure();
        figure.setName(figureRequest.name());
        figure.setCreatedBy(currentUser);
        figure.setCreatedAt(Instant.now());
        figure.setImageUrl(imageUrl);

        // RAG servisine gönderebilmek için ID alması amacıyla figürü önce kaydediyoruz.
        // Hata durumunda bu kayıt geri alınacaktır (rollback).
        HistoricalFigure savedFigure = historicalFigureRepository.save(figure);

        try {
            log.info("Sending document to RAG for validation and data extraction. Figure: {}", savedFigure.getName());
            RagDocumentRequest request = new RagDocumentRequest(tempDocPath, savedFigure.getId(), savedFigure.getName(),
                    currentUser.getId());
            RagUploadResponse response = ragServiceGateway.sendDocument(request);

            log.info("Successfully processed by RAG. Updating figure data.");
            updateFigureWithRagData(savedFigure, response); // Figür bilgilerini RAG'den gelenlerle güncelle

            Document document = new Document();
            document.setHistoricalFigure(savedFigure);
            document.setDocName(docFile.getOriginalFilename());
            document.setFilePath(tempDocPath.toString()); // Dosya yolu
            document.setCreatedAt(Instant.now());
            documentRepository.save(document);

            return historicalFigureMapper.toDto(savedFigure);

        } catch (Exception e) {
            log.error("Failed to process document with RAG for figure {}. Rolling back transaction. Error: {}",
                    figureRequest.name(), e.getMessage());
            throw new RagValidationException("Karakter oluşturma başarısız oldu: " + e.getMessage());
        }
    }

    public CharacterCardDto getCharacterCard(Long figureId) {
        HistoricalFigure figure = historicalFigureRepository.findById(figureId)
                .orElseThrow(() -> new RuntimeException("Figure not found with id: " + figureId));
        return historicalFigureMapper.toCharacterCardDto(figure);
    }

    @Transactional
    public DocumentDto addDocumentToOwnedFigure(Long figureId, MultipartFile file, String currentUserEmail)
            throws IOException {
        HistoricalFigure figure = findFigureByIdAndUser(figureId, currentUserEmail);
        Path tempDocPath = fileStorageService.storeFileAsTemp(file);

        try {
            log.info("Sending new document to RAG for validation and ingestion. Figure ID: {}", figureId);
            RagDocumentRequest request = new RagDocumentRequest(tempDocPath, figure.getId(), figure.getName(),
                    figure.getCreatedBy().getId());
            ragServiceGateway.sendDocument(request); // Sadece doğrulama ve RAG'e yükleme için çağırıyoruz.

            log.info("RAG processing successful. Saving document to database.");
            Document document = new Document();
            document.setHistoricalFigure(figure);
            document.setDocName(file.getOriginalFilename());
            document.setFilePath(tempDocPath.toString());
            document.setCreatedAt(Instant.now());
            Document savedDocument = documentRepository.save(document);

            return new DocumentDto(savedDocument.getId(), savedDocument.getDocName(),
                    savedDocument.getHistoricalFigure().getId(), savedDocument.getCreatedAt());

        } catch (Exception e) {
            log.error("Failed to add document for figure ID {}. Rolling back. Error: {}", figureId, e.getMessage());
            throw new RuntimeException("Doküman ekleme başarısız oldu: " + e.getMessage(), e);
        }
    }

    // --- ADMIN İŞLEMLERİ ---

    @Transactional
    public DocumentDto addDocumentAsAdmin(Long figureId, MultipartFile file) throws IOException {
        HistoricalFigure figure = historicalFigureRepository.findById(figureId)
                .orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));

        Path tempDocPath = fileStorageService.storeFileAsTemp(file);

        try {
            log.info("[ADMIN] Sending new document to RAG for validation and ingestion. Figure ID: {}", figureId);
            RagDocumentRequest request = new RagDocumentRequest(tempDocPath, figure.getId(), figure.getName(),
                    figure.getCreatedBy().getId());
            ragServiceGateway.sendDocument(request);

            log.info("[ADMIN] RAG processing successful. Saving document to database.");
            Document document = new Document();
            document.setHistoricalFigure(figure);
            document.setDocName(file.getOriginalFilename());
            document.setFilePath(tempDocPath.toString());
            document.setCreatedAt(Instant.now());
            Document savedDocument = documentRepository.save(document);

            return new DocumentDto(savedDocument.getId(), savedDocument.getDocName(),
                    savedDocument.getHistoricalFigure().getId(), savedDocument.getCreatedAt());
        } catch (Exception e) {
            log.error("[ADMIN] Failed to add document for figure ID {}. Rolling back. Error: {}", figureId,
                    e.getMessage());
            throw new RuntimeException("Doküman ekleme başarısız oldu: " + e.getMessage(), e);
        }
    }

    // --- OKUMA VE SİLME İŞLEMLERİ  ---

    @Transactional(readOnly = true)
    public Page<HistoricalFigureDto> findVisibleFiguresForUser(String currentUserEmail, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);
        Page<HistoricalFigure> figuresPage = historicalFigureRepository.findFiguresForUserView(currentUser, UserRole.ROLE_ADMIN, pageable);
        return figuresPage.map(historicalFigureMapper::toDto);
    }

    @Transactional(readOnly = true)
    public HistoricalFigureDto getFigureByIdForUser(Long figureId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = historicalFigureRepository.findById(figureId)
                .orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));

        boolean isOwner = figure.getCreatedBy().equals(currentUser);
        boolean isAdminCreated = figure.getCreatedBy().getRole() == UserRole.ROLE_ADMIN;

        if (isOwner || isAdminCreated) {
            return historicalFigureMapper.toDto(figure);
        } else {
            throw new ResourceNotFoundException("Historical Figure not found with id: " + figureId);
        }
    }

    @Transactional(readOnly = true)
    public Page<HistoricalFigureDto> getAllFiguresAsAdmin(Pageable pageable) {
        return historicalFigureRepository.findAll(pageable)
                .map(historicalFigureMapper::toDto);
    }

    @Transactional
    public void deleteFigureAsAdmin(Long figureId) {
        HistoricalFigure figureToDelete = historicalFigureRepository.findById(figureId)
                .orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));
        performFigureDeletion(figureToDelete);
    }

    @Transactional
    public void deleteFigureForUser(Long figureId, String currentUserEmail) {
        HistoricalFigure figureToDelete = findFigureByIdAndUser(figureId, currentUserEmail);
        performFigureDeletion(figureToDelete);
    }

    // --- YARDIMCI METOTLAR ---

    private void performFigureDeletion(HistoricalFigure figure) {
        try {
            log.info("Deleting RAG collection for figureId: {}", figure.getId());
            ragServiceGateway.deleteCollection(String.valueOf(figure.getId()));
            log.info("Successfully initiated deletion of RAG collection for figureId: {}", figure.getId());
        } catch (Exception e) {
            log.error("Failed to delete RAG collection for figureId: {}. Continuing with local deletion.",
                    figure.getId(), e);
        }
        documentRepository.deleteByHistoricalFigure(figure);
        historicalFigureRepository.delete(figure);
    }

    private void updateFigureWithRagData(HistoricalFigure figureToUpdate, RagUploadResponse response) {
        log.info("Updating figure '{}' with RAG data...", figureToUpdate.getName());
        figureToUpdate.setBirthDate(response.figureInfo().birthDate());
        figureToUpdate.setDeathDate(response.figureInfo().deathDate());
        figureToUpdate.setBio(response.figureInfo().bio());
        figureToUpdate.setRegion(response.figureInfo().region());
        historicalFigureRepository.save(figureToUpdate);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private HistoricalFigure findFigureByIdAndUser(Long figureId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return historicalFigureRepository.findByIdAndCreatedBy(figureId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historical Figure not found with id: " + figureId + " for this user."));
    }
}
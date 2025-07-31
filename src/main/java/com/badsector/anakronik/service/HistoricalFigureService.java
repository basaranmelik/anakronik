package com.badsector.anakronik.service;

import com.badsector.anakronik.controller.dto.CreateHistoricalFigureRequest;
import com.badsector.anakronik.dto.*;
import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
import com.badsector.anakronik.gateway.dto.RagUploadResponse;
import com.badsector.anakronik.mapper.HistoricalFigureMapper;
import com.badsector.anakronik.model.Document;
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.WorldRegion;
import com.badsector.anakronik.model.UserRole;
import com.badsector.anakronik.repository.DocumentRepository;
import com.badsector.anakronik.repository.HistoricalFigureRepository;
import com.badsector.anakronik.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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

    @Transactional
    public HistoricalFigureDto createFigureAndFirstDocument(CreateHistoricalFigureRequest figureRequest, MultipartFile file, String currentUserEmail) throws IOException {
        User currentUser = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = new HistoricalFigure();
        figure.setName(figureRequest.name());
        figure.setBio(figureRequest.bio());
        figure.setCreatedBy(currentUser);
        figure.setCreatedAt(Instant.now());
        HistoricalFigure savedFigure = historicalFigureRepository.save(figure);

        Path filePath = fileStorageService.storeFileAsTemp(file);

        Document document = new Document();
        document.setHistoricalFigure(savedFigure);
        document.setDocName(file.getOriginalFilename());
        document.setFilePath(filePath.toString());
        document.setCreatedAt(Instant.now());
        documentRepository.save(document);

        processDocumentWithRag(filePath, savedFigure.getId(), savedFigure.getName(), currentUser.getId());

        return historicalFigureMapper.toDto(savedFigure);
    }

    @Transactional
    public Document addDocumentToOwnedFigure(Long figureId, MultipartFile file, String currentUserEmail) throws IOException {
        HistoricalFigure figure = findFigureByIdAndUser(figureId, currentUserEmail);
        Path filePath = fileStorageService.storeFileAsTemp(file);
        Document document = new Document();
        document.setHistoricalFigure(figure);
        document.setDocName(file.getOriginalFilename());
        document.setFilePath(filePath.toString());
        document.setCreatedAt(Instant.now());
        Document savedDocument = documentRepository.save(document);

        processDocumentWithRag(filePath, figure.getId(), figure.getName(), figure.getCreatedBy().getId());

        return savedDocument;
    }

    @Async
    public void processDocumentWithRag(Path filePath, Long figureId, String characterName, Long userId) {
        try {
            log.info("Sending document to RAG service for figureId: {}", figureId);
            RagDocumentRequest request = new RagDocumentRequest(filePath, figureId, characterName, userId);
            RagUploadResponse response = ragServiceGateway.sendDocument(request);

            if (response != null && "ok".equals(response.status())) {
                log.info("Successfully received response for figureId: {}. Updating figure data.", figureId);
                updateFigureWithRagData(figureId, userId, response);
            } else {
                String errorMessage = (response != null) ? response.message() : "No response from service.";
                log.error("Failed to process document for figureId: {}. Reason: {}", figureId, errorMessage);
            }
        } catch (Exception e) {
            log.error("An exception occurred while processing document for figureId: {}. Error: {}", figureId, e.getMessage(), e);
        }
    }

    @Transactional
    public void updateFigureWithRagData(Long figureId, Long userId, RagUploadResponse response) {
        User user = findUserById(userId);
        HistoricalFigure figureToUpdate = findFigureByIdAndUser(figureId, user);
        log.info("Found figure '{}' for user '{}'. Updating with RAG data...", figureToUpdate.getName(), user.getUsername());

        figureToUpdate.setBirthDate(response.figureInfo().birthDate());
        figureToUpdate.setDeathDate(response.figureInfo().deathDate());
        figureToUpdate.setRegion(WorldRegion.fromTurkishName(response.figureInfo().region()));

        historicalFigureRepository.save(figureToUpdate);
        log.info("Figure '{}' updated successfully with RAG data.", figureToUpdate.getName());
    }

    // --- Diğer CRUD ve Yardımcı Metotlar ---
    @Transactional(readOnly = true)
    public Page<HistoricalFigureDto> findFiguresByUser(String currentUserEmail, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);
        Page<HistoricalFigure> figuresPage = historicalFigureRepository.findByCreatedBy(currentUser, pageable);
        return figuresPage.map(historicalFigureMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<HistoricalFigureDto> findVisibleFiguresForUser(String currentUserEmail, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);
        // Yeni repository metodunu kullanıyoruz.
        Page<HistoricalFigure> figuresPage = historicalFigureRepository.findFiguresForUserView(currentUser, UserRole.ROLE_ADMIN, pageable);
        return figuresPage.map(historicalFigureMapper::toDto);
    }


    @Transactional(readOnly = true)
    public HistoricalFigureDto getFigureByIdForUser(Long figureId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = historicalFigureRepository.findById(figureId)
                .orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));

        // Görünürlük kontrolü: Figür ya kullanıcıya aittir YA DA bir admin tarafından oluşturulmuştur.
        boolean isOwner = figure.getCreatedBy().equals(currentUser);
        boolean isAdminCreated = figure.getCreatedBy().getRole() == UserRole.ROLE_ADMIN;

        if (isOwner || isAdminCreated) {
            return historicalFigureMapper.toDto(figure);
        } else {
            // Kullanıcının bu figürü görme yetkisi yok.
            // Güvenlik açısından "bulunamadı" hatası dönmek daha iyidir.
            throw new ResourceNotFoundException("Historical Figure not found with id: " + figureId);
        }
    }

    @Transactional
    public HistoricalFigureDto updateFigureForUser(Long figureId, CreateHistoricalFigureRequest request, String currentUserEmail) {
        HistoricalFigure figureToUpdate = findFigureByIdAndUser(figureId, currentUserEmail);
        figureToUpdate.setName(request.name());
        figureToUpdate.setBio(request.bio());
        HistoricalFigure updatedFigure = historicalFigureRepository.save(figureToUpdate);
        return historicalFigureMapper.toDto(updatedFigure);
    }

    @Transactional
    public void deleteFigureForUser(Long figureId, String currentUserEmail) {
        HistoricalFigure figureToDelete = findFigureByIdAndUser(figureId, currentUserEmail);

        try {
            log.info("Deleting RAG collection for figureId: {}", figureId);
            String collectionName = String.valueOf(figureId);
            ragServiceGateway.deleteCollection(collectionName);
            log.info("Successfully initiated deletion of RAG collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to delete RAG collection for figureId: {}. Continuing with local deletion.", figureId, e);
        }
        documentRepository.deleteByHistoricalFigure(figureToDelete);
        historicalFigureRepository.delete(figureToDelete);
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    private HistoricalFigure findFigureByIdAndUser(Long figureId, User user) {
        return historicalFigureRepository.findByIdAndCreatedBy(figureId, user).orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));
    }

    private HistoricalFigure findFigureByIdAndUser(Long figureId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return findFigureByIdAndUser(figureId, currentUser);
    }
}
package com.badsector.anakronik.service;

import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
import com.badsector.anakronik.gateway.dto.RagUploadResponse;
import com.badsector.anakronik.gateway.dto.ask.ChatRequest;   // <-- YENİ IMPORT
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;  // <-- YENİ IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class RagProcessingService {

    private static final Logger log = LoggerFactory.getLogger(RagProcessingService.class);
    private final RagServiceGatewayImpl ragServiceGateway;
    private final HistoricalFigureService historicalFigureService;

    public RagProcessingService(RagServiceGatewayImpl ragServiceGateway,
                                @Lazy HistoricalFigureService historicalFigureService) {
        this.ragServiceGateway = ragServiceGateway;
        this.historicalFigureService = historicalFigureService;
    }

    /**
     * Bir belgeyi RAG servisine ASENKRON olarak gönderir ve sonucu veritabanına işler.
     */
    @Async
    public void sendDocumentToRag(Path filePath, Long figureId, String characterName, Long userId) {
        try {
            log.info("Sending document to RAG service for figureId: {}", figureId);
            RagDocumentRequest request = new RagDocumentRequest(filePath, figureId, characterName, userId);
            RagUploadResponse response = ragServiceGateway.sendDocument(request);

            if (response != null && "ok".equals(response.status())) {
                log.info("Successfully received response for figureId: {}. Delegating update to HistoricalFigureService.", figureId);
                historicalFigureService.updateFigureWithRagData(figureId, userId, response);

            } else {
                String errorMessage = (response != null) ? response.message() : "No response from service.";
                log.error("Failed to process document for figureId: {}. Reason: {}", figureId, errorMessage);
            }
        } catch (Exception e) {
            log.error("An exception occurred while processing document for figureId: {}. Error: {}", figureId, e.getMessage(), e);
        }
    }

    /**
     * Bir sohbet sorusunu RAG servisine SENKRON olarak gönderir ve yanıtı döndürür.
     */
    public ChatResponse askQuestion(ChatRequest request) {
        log.info("Sending question to RAG service for figureId: {}", request.historicalFigureId());
        try {
            return ragServiceGateway.askQuestion(request);
        } catch (Exception e) {
            log.error("Failed to get answer from RAG service for figureId: {}. Error: {}", request.historicalFigureId(), e.getMessage());
            // Hata, bu metodu çağıran bir üst katmanda (örn: ChatService) yönetilmelidir.
            throw new RuntimeException("Error while communicating with the chat service.", e);
        }
    }
}
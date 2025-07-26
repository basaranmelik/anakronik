// com.badsector.anakronik.service.RagProcessingService.java
package com.badsector.anakronik.service;

import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.nio.file.Path;

@Service
public class RagProcessingService {

    private static final Logger log = LoggerFactory.getLogger(RagProcessingService.class);
    private final RagServiceGatewayImpl ragServiceGateway;

    public RagProcessingService(RagServiceGatewayImpl ragServiceGateway) {
        this.ragServiceGateway = ragServiceGateway;
    }

    @Async
    public void sendDocumentToRag(Path filePath, String docName, Long figureId, String characterName, Long userId) {
        try {
            log.info("Sending document to RAG service for figureId: {}", figureId);

            // DEĞİŞİKLİK: Tek bir request nesnesi oluşturuyoruz.
            RagDocumentRequest request = new RagDocumentRequest(
                    filePath,
                    docName,
                    figureId,
                    characterName,
                    userId
            );

            // DEĞİŞİKLİK: Gateway'i yeni nesne ile çağırıyoruz.
            ragServiceGateway.sendDocument(request);

            log.info("Successfully sent document to RAG service for figureId: {}", figureId);
        } catch (Exception e) {
            log.error("Failed to send document to RAG service for figureId: {}. Error: {}", figureId, e.getMessage());
        }
    }
}

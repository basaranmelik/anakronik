package com.badsector.anakronik.gateway;

import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
import com.badsector.anakronik.gateway.dto.RagUploadResponse; // <-- YENİ IMPORT
import com.badsector.anakronik.gateway.dto.ask.ChatRequest;
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RagServiceGatewayImpl {

    @Value("${external.service.document.upload-url}")
    private String externalServiceUploadUrl;
    @Value("${external.service.chat.ask-url}")
    private String externalServiceAskUrl;

    private final RestTemplate externalServiceRestTemplate;

    public RagServiceGatewayImpl(@Qualifier("ragServiceClient") RestTemplate externalServiceRestTemplate) {
        this.externalServiceRestTemplate = externalServiceRestTemplate;
    }
    public RagUploadResponse sendDocument(RagDocumentRequest request) throws IOException { // <-- DÖNÜŞ TİPİ DEĞİŞTİ
        Path filePath = request.file();
        if (!Files.exists(filePath)) {
            throw new IOException("Document file not found at path: " + filePath);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath.toFile()));
        body.add("user_id", request.userId().toString());
        body.add("historical_figure_name", request.historicalFigureName());
        body.add("historical_figure_id", request.historicalFigureId().toString());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<RagUploadResponse> response = externalServiceRestTemplate.exchange(
                    externalServiceUploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    RagUploadResponse.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "External service endpoint not found.", e);
            } else if (e.getMessage() != null && e.getMessage().contains("500")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "External service encountered an error.", e);
            }
            throw new RuntimeException("Failed to send document to external service: " + e.getMessage(), e);
        }
    }
    public ChatResponse askQuestion(ChatRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ChatResponse> response = externalServiceRestTemplate.exchange(
                    externalServiceAskUrl,
                    HttpMethod.POST,
                    requestEntity,
                    ChatResponse.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            // Hata yönetimi, ihtiyaca göre özelleştirilebilir.
            throw new RuntimeException("Failed to get answer from external service: " + e.getMessage(), e);
        }
    }

}
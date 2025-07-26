package com.badsector.anakronik.gateway;

import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
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

    private final RestTemplate externalServiceRestTemplate;

    public RagServiceGatewayImpl(@Qualifier("ragServiceClient") RestTemplate externalServiceRestTemplate) {
        this.externalServiceRestTemplate = externalServiceRestTemplate;
    }

    /**
     * Belirtilen bilgileri içeren request nesnesini kullanarak dış servise belge gönderir.
     *
     * @param request Gönderilecek dosya ve meta verileri içeren nesne.
     * @throws IOException             Dosya okuma sırasında bir hata oluşursa.
     * @throws ResponseStatusException Dış servis hata kodu döndürürse.
     */
    public void sendDocument(RagDocumentRequest request) throws IOException { // <-- DEĞİŞİKLİK BURADA
        Path filePath = request.filePath(); // Verileri request nesnesinden alıyoruz
        if (!Files.exists(filePath)) {
            throw new IOException("Document file not found at path: " + filePath);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath.toFile()));
        body.add("user_id", request.userId().toString());
        body.add("character_name", request.characterName());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            externalServiceRestTemplate.exchange(
                    externalServiceUploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
        } catch (RestClientException e) {
            // Hata yönetimi aynı kalabilir.
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "External service endpoint not found or resource not available.", e);
            } else if (e.getMessage() != null && e.getMessage().contains("500")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "External service encountered an internal error.", e);
            }
            throw new RuntimeException("Failed to send document to external service: " + e.getMessage(), e);
        }
    }
}
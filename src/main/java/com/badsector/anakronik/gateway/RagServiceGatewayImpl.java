package com.badsector.anakronik.gateway;

import com.badsector.anakronik.exception.RagServiceException;
import com.badsector.anakronik.gateway.dto.RagDocumentRequest;
import com.badsector.anakronik.gateway.dto.RagUploadResponse;
import com.badsector.anakronik.gateway.dto.ask.ChatRequest;
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RagServiceGatewayImpl {

    private final WebClient ragWebClient;

    public RagServiceGatewayImpl(@Qualifier("ragWebClient") WebClient ragWebClient) {
        this.ragWebClient = ragWebClient;
    }

    public RagUploadResponse sendDocument(RagDocumentRequest request) throws IOException {
        Path filePath = request.file();
        if (!Files.exists(filePath)) {
            throw new IOException("Document file not found at path: " + filePath);
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(filePath));
        builder.part("user_id", request.userId());
        builder.part("historical_figure_name", request.historicalFigureName());
        builder.part("historical_figure_id", request.historicalFigureId());

        try {
            return ragWebClient.post()
                    .uri("/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(RagUploadResponse.class)
                    .doOnNext(response -> { // Yanıtı kontrol et
                        if (response == null || !"ok".equals(response.status())) {
                            String errorMessage = (response != null) ? response.message() : "RAG service returned an empty or invalid response.";
                            // Mantıksal hata varsa custom exception fırlat
                            throw new RagServiceException(errorMessage);
                        }
                    })
                    .block(); // block() metodu, exception'ı burada fırlatacaktır.
        } finally {
            deleteTempFile(filePath);
        }
    }

    public ChatResponse askQuestion(ChatRequest request) {
        return ragWebClient.post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "External service endpoint not found.")))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "External service encountered an error.")))
                .bodyToMono(ChatResponse.class)
                .block();
    }

    public void deleteCollection(String collectionName) {
        ragWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/collections/{name}").build(collectionName))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ResponseStatusException(response.statusCode(), "External service error during deletion: " + errorBody))))
                .bodyToMono(Void.class)
                .block();
    }

    private void deleteTempFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // log.error("Could not delete temporary file: {}", filePath, e);
        }
    }
}
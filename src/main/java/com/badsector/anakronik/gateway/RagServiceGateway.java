package com.badsector.anakronik.gateway;

import com.badsector.anakronik.gateway.dto.RagDocumentRequest;

import java.nio.file.Path;

public interface RagServiceGateway {
    void sendDocument(RagDocumentRequest ragDocumentRequest);
}

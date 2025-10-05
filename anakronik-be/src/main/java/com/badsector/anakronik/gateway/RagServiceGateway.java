package com.badsector.anakronik.gateway;

import com.badsector.anakronik.gateway.dto.RagDocumentRequest;

public interface RagServiceGateway {
    void sendDocument(RagDocumentRequest ragDocumentRequest);
}

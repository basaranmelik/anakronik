package com.badsector.anakronik.gateway.dto;

import java.nio.file.Path;

/**
 * RAG servisine belge gönderimi için gerekli tüm bilgileri taşıyan bir veri nesnesi.
 */
public record RagDocumentRequest(
        Path filePath,
        String docName,
        Long historicalFigureId,
        String characterName,
        Long userId
) {}
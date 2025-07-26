package com.badsector.anakronik.dto;
import org.springframework.web.multipart.MultipartFile;

public record AddDocumentRequest(String docName, MultipartFile file) {}

package com.badsector.anakronik.controller.dto;
import org.springframework.web.multipart.MultipartFile;

public record AddDocumentRequest(String docName, MultipartFile file) {}

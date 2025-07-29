package com.badsector.anakronik.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    public FileStorageService() {
    }

    public Path storeFileAsTemp(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String prefix = originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) : originalFilename;
        String suffix = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";

        Path tempFile = Files.createTempFile(prefix + "_", suffix);

        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }
}
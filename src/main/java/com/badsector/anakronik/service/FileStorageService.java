package com.badsector.anakronik.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path imageStorageLocation;

    public FileStorageService(@Value("${app.figure.image-dir}") String imageDir) {
        this.imageStorageLocation = Paths.get(imageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.imageStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the image storage directory.", ex);
        }
    }

    public Path storeFileAsTemp(MultipartFile file) throws IOException {
        if (file.isEmpty()) { throw new IOException("Failed to store empty file."); }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String prefix = originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) : originalFilename;
        String suffix = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        Path tempFile = Files.createTempFile(prefix + "_", suffix);
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    public String storeImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) { throw new IOException("Failed to store empty image file."); }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.imageStorageLocation.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return "/images/" + uniqueFileName;
    }
}
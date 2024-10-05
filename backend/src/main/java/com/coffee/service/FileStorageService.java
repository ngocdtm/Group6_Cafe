package com.coffee.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileStorageService {

    private final String uploadDir;

    @Autowired
    public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Value("${app.file.allowed-types}")
    private List<String> allowedTypes;

    @Value("${app.file.max-size}")
    private long maxFileSize;


    public String storeFile(MultipartFile file, String productId) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique filename
            String fileName = generateUniqueFileName(productId, file.getOriginalFilename());

            // Get absolute path
            Path targetLocation = getUploadPath().resolve(fileName);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Stored file " + fileName + " successfully");
            return fileName;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum limit");
        }

        // Check file type
        String contentType = file.getContentType();
        if(contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new FileStorageException("File type not allowed");
        }
    }

    private String generateUniqueFileName(String productId, String originalFileName) {
        String fileExtension = StringUtils.getFilenameExtension(originalFileName);
        return String.format("%s_%s.%s", productId, System.currentTimeMillis(), fileExtension);
    }

    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = getUploadPath().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new FileStorageException("File not found: " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = getUploadPath().resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            System.out.println("Deleted file {} successfully" + fileName);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + fileName, ex);
        }
    }
}


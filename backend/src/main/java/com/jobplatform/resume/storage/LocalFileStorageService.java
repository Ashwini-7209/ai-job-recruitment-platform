package com.jobplatform.resume.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path storageLocation;

    public LocalFileStorageService(@Value("${app.storage.resume-directory:uploads/resumes}") String storageDir) {
        this.storageLocation = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + this.storageLocation, e);
        }
    }

    @Override
    public String store(byte[] data, String storageKey) {
        try {
            Path targetLocation = this.storageLocation.resolve(storageKey);
            Files.copy(new ByteArrayInputStream(data), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {}", storageKey);
            return targetLocation.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + storageKey, e);
        }
    }

    @Override
    public Optional<InputStream> load(String storageKey) {
        try {
            Path filePath = this.storageLocation.resolve(storageKey);
            if (!Files.exists(filePath)) {
                return Optional.empty();
            }
            return Optional.of(Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path filePath = this.storageLocation.resolve(storageKey);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", storageKey);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storageKey, e);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        Path filePath = this.storageLocation.resolve(storageKey);
        return Files.exists(filePath);
    }
}

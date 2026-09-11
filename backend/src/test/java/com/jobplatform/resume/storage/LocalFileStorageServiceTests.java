package com.jobplatform.resume.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileStorageServiceTests {

    private LocalFileStorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(tempDir.toString());
    }

    @Test
    void store_createsFile() {
        byte[] data = "test content".getBytes();

        storageService.store(data, "test-file.pdf");

        Path filePath = tempDir.resolve("test-file.pdf");
        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    void store_returnsCorrectPath() {
        byte[] data = "test content".getBytes();

        String path = storageService.store(data, "test-file.pdf");

        assertThat(path).contains("test-file.pdf");
    }

    @Test
    void load_returnsInputStream() {
        byte[] data = "test content".getBytes();
        storageService.store(data, "test-file.pdf");

        Optional<InputStream> stream = storageService.load("test-file.pdf");

        assertThat(stream).isPresent();
    }

    @Test
    void load_returnsEmptyForMissingFile() {
        Optional<InputStream> stream = storageService.load("non-existent.pdf");

        assertThat(stream).isEmpty();
    }

    @Test
    void delete_removesFile() {
        byte[] data = "test content".getBytes();
        storageService.store(data, "test-file.pdf");

        storageService.delete("test-file.pdf");

        assertThat(Files.exists(tempDir.resolve("test-file.pdf"))).isFalse();
    }

    @Test
    void delete_handlesNonExistentFile() {
        storageService.delete("non-existent.pdf");
    }

    @Test
    void exists_returnsTrueForStoredFile() {
        byte[] data = "test content".getBytes();
        storageService.store(data, "test-file.pdf");

        assertThat(storageService.exists("test-file.pdf")).isTrue();
    }

    @Test
    void exists_returnsFalseForMissingFile() {
        assertThat(storageService.exists("non-existent.pdf")).isFalse();
    }
}

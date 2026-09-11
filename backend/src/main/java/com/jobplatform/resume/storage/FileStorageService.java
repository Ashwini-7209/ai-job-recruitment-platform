package com.jobplatform.resume.storage;

import java.io.InputStream;
import java.util.Optional;

public interface FileStorageService {

    String store(byte[] data, String storageKey);

    Optional<InputStream> load(String storageKey);

    void delete(String storageKey);

    boolean exists(String storageKey);
}

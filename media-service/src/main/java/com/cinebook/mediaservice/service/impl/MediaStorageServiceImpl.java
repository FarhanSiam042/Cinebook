package com.cinebook.mediaservice.service.impl;

import com.cinebook.mediaservice.config.MediaProperties;
import com.cinebook.mediaservice.dto.MediaResponse;
import com.cinebook.mediaservice.exception.InvalidOperationException;
import com.cinebook.mediaservice.exception.ResourceNotFoundException;
import com.cinebook.mediaservice.service.MediaStorageService;
import com.cinebook.mediaservice.service.StoredFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaStorageServiceImpl implements MediaStorageService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp",
            "image/gif", "gif");

    private final MediaProperties properties;

    private Path storageDir;

    @PostConstruct
    void init() {
        storageDir = Paths.get(properties.storageLocation()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageDir);
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not create media storage directory: " + storageDir, e);
        }
    }

    @Override
    public MediaResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("No file was uploaded");
        }
        String contentType = file.getContentType();
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new InvalidOperationException(
                    "Unsupported image type: " + contentType + ". Allowed types: " + ALLOWED_TYPES.keySet());
        }

        String id = UUID.randomUUID() + "." + extension;
        Path target = storageDir.resolve(id);
        try {
            file.transferTo(target);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }

        return MediaResponse.builder()
                .id(id)
                .url(properties.publicBaseUrl() + "/api/media/images/" + id)
                .contentType(contentType)
                .size(file.getSize())
                .build();
    }

    @Override
    public StoredFile load(String id) {
        Path path = resolveSafely(id);
        if (!Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Media not found with id: " + id);
        }
        try {
            byte[] content = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            return new StoredFile(content, contentType != null ? contentType : "application/octet-stream");
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read stored media file", e);
        }
    }

    @Override
    public void delete(String id) {
        Path path = resolveSafely(id);
        try {
            if (!Files.deleteIfExists(path)) {
                throw new ResourceNotFoundException("Media not found with id: " + id);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to delete stored media file", e);
        }
    }

    // resolves an id to a path inside storageDir, rejecting any attempt to escape it (e.g. "../../secrets")
    private Path resolveSafely(String id) {
        Path resolved = storageDir.resolve(id).normalize();
        if (!resolved.startsWith(storageDir) || !ALLOWED_TYPES.containsValue(extensionOf(id))) {
            throw new ResourceNotFoundException("Media not found with id: " + id);
        }
        return resolved;
    }

    private String extensionOf(String id) {
        int dot = id.lastIndexOf('.');
        return dot >= 0 ? id.substring(dot + 1) : "";
    }
}

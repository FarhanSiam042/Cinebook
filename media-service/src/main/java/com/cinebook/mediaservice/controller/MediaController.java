package com.cinebook.mediaservice.controller;

import com.cinebook.mediaservice.dto.MediaResponse;
import com.cinebook.mediaservice.service.MediaStorageService;
import com.cinebook.mediaservice.service.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image (poster, backdrop, etc.) — ADMIN only", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MediaResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(mediaStorageService.store(file));
    }

    @GetMapping("/images/{id}")
    @Operation(summary = "Fetch a previously uploaded image — public", security = {})
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        StoredFile stored = mediaStorageService.load(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(stored.contentType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(stored.content());
    }

    @DeleteMapping("/images/{id}")
    @Operation(summary = "Delete an uploaded image — ADMIN only", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable String id) {
        mediaStorageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.cinebook.mediaservice.service;

import com.cinebook.mediaservice.dto.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    MediaResponse store(MultipartFile file);

    StoredFile load(String id);

    void delete(String id);
}

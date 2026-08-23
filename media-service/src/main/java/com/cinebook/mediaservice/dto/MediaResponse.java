package com.cinebook.mediaservice.dto;

import lombok.Builder;

@Builder
public record MediaResponse(String id, String url, String contentType, long size) {
}

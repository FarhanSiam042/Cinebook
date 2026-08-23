package com.cinebook.mediaservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public record MediaProperties(String storageLocation, String publicBaseUrl) {
}

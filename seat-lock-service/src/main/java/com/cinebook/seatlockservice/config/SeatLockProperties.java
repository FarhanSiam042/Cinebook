package com.cinebook.seatlockservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seatlock")
public record SeatLockProperties(long holdTtlSeconds) {
}

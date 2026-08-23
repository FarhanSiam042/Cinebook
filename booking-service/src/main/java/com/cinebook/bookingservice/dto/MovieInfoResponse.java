package com.cinebook.bookingservice.dto;

import java.math.BigDecimal;

public record MovieInfoResponse(Long id, String title, BigDecimal ticketPrice) {
}

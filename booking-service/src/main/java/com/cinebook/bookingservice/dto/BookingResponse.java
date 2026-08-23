package com.cinebook.bookingservice.dto;

import com.cinebook.bookingservice.model.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
		Long id,
		String bookingReference,
		String customerName,
		String customerEmail,
		Long movieId,
		String movieTitle,
		Long theaterId,
		String theaterName,
		LocalDateTime showTime,
		Integer seatCount,
		BigDecimal amount,
		BookingStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}

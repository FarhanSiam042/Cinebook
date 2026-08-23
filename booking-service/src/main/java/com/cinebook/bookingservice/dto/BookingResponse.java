package com.cinebook.bookingservice.dto;

import com.cinebook.bookingservice.model.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
		List<Long> seatIds,
		List<String> seatLabels,
		BigDecimal amount,
		BookingStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}

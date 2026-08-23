package com.cinebook.bookingservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingConfirmedEvent(
		Long bookingId,
		String bookingReference,
		String customerName,
		String customerEmail,
		Long movieId,
		String movieTitle,
		Long theaterId,
		String theaterName,
		LocalDateTime showTime,
		Integer seatCount,
		BigDecimal amount
) {
}

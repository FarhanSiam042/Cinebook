package com.cinebook.bookingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeInfoResponse(
		Long id,
		Long movieId,
		Long theaterId,
		Long screenId,
		LocalDateTime startTime,
		BigDecimal price
) {
}

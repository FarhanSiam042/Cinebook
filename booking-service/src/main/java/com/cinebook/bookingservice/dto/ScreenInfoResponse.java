package com.cinebook.bookingservice.dto;

public record ScreenInfoResponse(
		Long id,
		Integer totalRows,
		Integer seatsPerRow
) {
}

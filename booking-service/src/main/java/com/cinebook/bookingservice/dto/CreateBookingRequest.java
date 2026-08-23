package com.cinebook.bookingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
		@NotBlank String customerName,
		@NotBlank String customerEmail,
		@NotNull Long showtimeId,
		@NotNull @Min(1) Integer seatCount
) {
}

package com.cinebook.theaterservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScreenRequest(
	@NotBlank @Size(max = 80) String name,
	@NotNull @Min(1) @Max(26) Integer totalRows,
	@NotNull @Min(1) @Max(60) Integer seatsPerRow,
	Boolean active
) {
}

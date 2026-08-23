package com.cinebook.theaterservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatCategoryRequest(
	@NotBlank @Size(max = 80) String name,
	@NotBlank @Size(max = 30) String code,
	@NotNull @DecimalMin("0.01") BigDecimal priceMultiplier,
	Boolean active
) {
}

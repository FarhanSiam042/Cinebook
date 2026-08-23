package com.cinebook.theaterservice.dto;

import java.math.BigDecimal;

public record SeatCategoryResponse(
	Long id,
	String name,
	String code,
	BigDecimal priceMultiplier,
	boolean active
) {
}

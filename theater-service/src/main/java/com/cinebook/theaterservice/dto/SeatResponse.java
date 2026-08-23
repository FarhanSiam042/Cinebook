package com.cinebook.theaterservice.dto;

import java.math.BigDecimal;

public record SeatResponse(
	Long id,
	Long theaterId,
	String theaterName,
	Long screenId,
	String screenName,
	String rowLabel,
	Integer seatNumber,
	Long categoryId,
	String categoryName,
	String categoryCode,
	BigDecimal priceMultiplier,
	boolean active
) {
}

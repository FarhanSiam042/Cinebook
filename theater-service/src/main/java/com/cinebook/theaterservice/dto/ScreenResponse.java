package com.cinebook.theaterservice.dto;

public record ScreenResponse(
	Long id,
	Long theaterId,
	String theaterName,
	String name,
	Integer totalRows,
	Integer seatsPerRow,
	boolean active
) {
}

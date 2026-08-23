package com.cinebook.theaterservice.dto;

public record TheaterResponse(
	Long id,
	String name,
	String city,
	String address,
	boolean active,
	Integer availableSeats
) {
}

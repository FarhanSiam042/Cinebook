package com.cinebook.theaterservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TheaterRequest(
	@NotBlank @Size(max = 120) String name,
	@NotBlank @Size(max = 80) String city,
	@NotBlank @Size(max = 255) String address,
	Boolean active
) {
}

package com.cinebook.theaterservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateSeatsRequest(
	@NotBlank @Size(max = 30) String categoryCode
) {
}

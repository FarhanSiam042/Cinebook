package com.cinebook.seatlockservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmHoldRequest(@NotBlank String bookingReference) {
}

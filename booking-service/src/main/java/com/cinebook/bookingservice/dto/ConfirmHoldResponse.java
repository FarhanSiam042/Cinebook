package com.cinebook.bookingservice.dto;

import java.util.List;

public record ConfirmHoldResponse(String holdToken, List<Long> seatIds, String bookingReference) {
}

package com.cinebook.paymentservice.dto;

import com.cinebook.paymentservice.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
		Long id,
		Long bookingId,
		String bookingReference,
		String customerEmail,
		BigDecimal amount,
		String paymentMethod,
		String transactionReference,
		PaymentStatus status,
		LocalDateTime createdAt,
		LocalDateTime processedAt
) {
}

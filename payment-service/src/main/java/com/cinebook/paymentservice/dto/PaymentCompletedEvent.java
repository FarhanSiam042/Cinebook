package com.cinebook.paymentservice.dto;

import com.cinebook.paymentservice.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
		Long paymentId,
		Long bookingId,
		String bookingReference,
		BigDecimal amount,
		String transactionReference,
		PaymentStatus status,
		LocalDateTime processedAt
) {
}

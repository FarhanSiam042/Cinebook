package com.cinebook.notificationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
		Long paymentId,
		Long bookingId,
		String bookingReference,
		BigDecimal amount,
		String transactionReference,
		String status,
		LocalDateTime processedAt
) {
}

package com.cinebook.notificationservice.dto;

import com.cinebook.notificationservice.model.NotificationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NotificationResponse(
		Long id,
		Long paymentId,
		Long bookingId,
		String bookingReference,
		String recipientEmail,
		BigDecimal amount,
		String transactionReference,
		String channel,
		String message,
		NotificationStatus status,
		LocalDateTime createdAt,
		LocalDateTime sentAt
) {
}

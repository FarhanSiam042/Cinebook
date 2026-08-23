package com.cinebook.notificationservice.service.impl;

import com.cinebook.notificationservice.dto.NotificationResponse;
import com.cinebook.notificationservice.dto.PaymentCompletedEvent;
import com.cinebook.notificationservice.exception.NotificationNotFoundException;
import com.cinebook.notificationservice.model.Notification;
import com.cinebook.notificationservice.model.NotificationStatus;
import com.cinebook.notificationservice.repository.NotificationRepository;
import com.cinebook.notificationservice.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	@Override
	@Transactional
	public NotificationResponse processPaymentCompleted(PaymentCompletedEvent event) {
		return notificationRepository.findByPaymentId(event.paymentId())
				.map(this::toResponse)
				.orElseGet(() -> toResponse(sendConfirmation(event)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationResponse> getAllNotifications() {
		return notificationRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public NotificationResponse getNotificationById(Long id) {
		return toResponse(notificationRepository.findById(id)
				.orElseThrow(() -> new NotificationNotFoundException("Notification not found for id: " + id)));
	}

	@Override
	@Transactional(readOnly = true)
	public NotificationResponse getNotificationByBookingReference(String bookingReference) {
		return toResponse(notificationRepository.findByBookingReference(bookingReference)
				.orElseThrow(() -> new NotificationNotFoundException(
						"Notification not found for booking reference: " + bookingReference)));
	}

	private Notification sendConfirmation(PaymentCompletedEvent event) {
		String message = "Payment of %s confirmed for booking %s (transaction %s). Your tickets are booked!"
				.formatted(event.amount(), event.bookingReference(), event.transactionReference());

		// Simulated delivery: this project has no real email/SMS provider wired up,
		// so "sending" a notification means logging it and persisting an audit record.
		log.info("Sending EMAIL notification to {} — {}", event.bookingReference(), message);

		Notification notification = Notification.builder()
				.paymentId(event.paymentId())
				.bookingId(event.bookingId())
				.bookingReference(event.bookingReference())
				.recipientEmail(resolveRecipientEmail(event))
				.amount(event.amount())
				.transactionReference(event.transactionReference())
				.channel("EMAIL")
				.message(message)
				.status(NotificationStatus.SENT)
				.sentAt(LocalDateTime.now())
				.build();
		return notificationRepository.save(notification);
	}

	private String resolveRecipientEmail(PaymentCompletedEvent event) {
		// payment.completed does not carry the customer's email; booking reference
		// is kept as the addressable key until the event contract is extended.
		return "booking-" + event.bookingReference() + "@cinebook.local";
	}

	private NotificationResponse toResponse(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getPaymentId(),
				notification.getBookingId(),
				notification.getBookingReference(),
				notification.getRecipientEmail(),
				notification.getAmount(),
				notification.getTransactionReference(),
				notification.getChannel(),
				notification.getMessage(),
				notification.getStatus(),
				notification.getCreatedAt(),
				notification.getSentAt()
		);
	}
}

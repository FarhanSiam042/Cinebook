package com.cinebook.paymentservice.service.impl;

import com.cinebook.paymentservice.config.RabbitConfig;
import com.cinebook.paymentservice.dto.BookingConfirmedEvent;
import com.cinebook.paymentservice.dto.PaymentCompletedEvent;
import com.cinebook.paymentservice.dto.PaymentResponse;
import com.cinebook.paymentservice.exception.PaymentNotFoundException;
import com.cinebook.paymentservice.model.Payment;
import com.cinebook.paymentservice.model.PaymentStatus;
import com.cinebook.paymentservice.repository.PaymentRepository;
import com.cinebook.paymentservice.service.PaymentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final RabbitTemplate rabbitTemplate;

	@Override
	@Transactional
	public PaymentResponse processBookingConfirmed(BookingConfirmedEvent event) {
		return paymentRepository.findByBookingReference(event.bookingReference())
				.map(existingPayment -> {
					if (existingPayment.getStatus() == PaymentStatus.COMPLETED) {
						return toResponse(existingPayment);
					}
					existingPayment.setStatus(PaymentStatus.COMPLETED);
					existingPayment.setProcessedAt(LocalDateTime.now());
					Payment updatedPayment = paymentRepository.save(existingPayment);
					publishPaymentCompleted(updatedPayment);
					return toResponse(updatedPayment);
				})
				.orElseGet(() -> {
					Payment payment = paymentRepository.save(Payment.builder()
							.bookingId(event.bookingId())
							.bookingReference(event.bookingReference())
							.customerEmail(event.customerEmail())
							.amount(event.amount())
							.paymentMethod("ONLINE")
							.transactionReference(UUID.randomUUID().toString())
							.status(PaymentStatus.COMPLETED)
							.processedAt(LocalDateTime.now())
							.build());
					publishPaymentCompleted(payment);
					return toResponse(payment);
				});
	}

	@Override
	@Transactional(readOnly = true)
	public List<PaymentResponse> getAllPayments() {
		return paymentRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentResponse getPaymentById(Long id) {
		return toResponse(paymentRepository.findById(id)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found for id: " + id)));
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentResponse getPaymentByBookingReference(String bookingReference) {
		return toResponse(paymentRepository.findByBookingReference(bookingReference)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found for booking reference: " + bookingReference)));
	}

	private void publishPaymentCompleted(Payment payment) {
		PaymentCompletedEvent event = new PaymentCompletedEvent(
				payment.getId(),
				payment.getBookingId(),
				payment.getBookingReference(),
				payment.getAmount(),
				payment.getTransactionReference(),
				payment.getStatus(),
				payment.getProcessedAt()
		);
		rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.PAYMENT_COMPLETED_QUEUE, event);
	}

	private PaymentResponse toResponse(Payment payment) {
		return new PaymentResponse(
				payment.getId(),
				payment.getBookingId(),
				payment.getBookingReference(),
				payment.getCustomerEmail(),
				payment.getAmount(),
				payment.getPaymentMethod(),
				payment.getTransactionReference(),
				payment.getStatus(),
				payment.getCreatedAt(),
				payment.getProcessedAt()
		);
	}
}

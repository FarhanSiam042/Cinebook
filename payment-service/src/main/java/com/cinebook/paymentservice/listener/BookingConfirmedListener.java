package com.cinebook.paymentservice.listener;

import com.cinebook.paymentservice.config.RabbitConfig;
import com.cinebook.paymentservice.dto.BookingConfirmedEvent;
import com.cinebook.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingConfirmedListener {

	private final PaymentService paymentService;

	@RabbitListener(queues = RabbitConfig.BOOKING_CONFIRMED_QUEUE)
	public void handleBookingConfirmed(BookingConfirmedEvent event) {
		paymentService.processBookingConfirmed(event);
	}
}

package com.cinebook.notificationservice.listener;

import com.cinebook.notificationservice.config.RabbitConfig;
import com.cinebook.notificationservice.dto.PaymentCompletedEvent;
import com.cinebook.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {

	private final NotificationService notificationService;

	@RabbitListener(queues = RabbitConfig.PAYMENT_COMPLETED_QUEUE)
	public void handlePaymentCompleted(PaymentCompletedEvent event) {
		notificationService.processPaymentCompleted(event);
	}
}

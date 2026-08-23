package com.cinebook.paymentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	public static final String EVENTS_EXCHANGE = "cinebook.events";
	public static final String BOOKING_CONFIRMED_QUEUE = "booking.confirmed";
	public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed";

	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	@Bean
	public DirectExchange eventsExchange() {
		return new DirectExchange(EVENTS_EXCHANGE, true, false);
	}

	@Bean
	public Queue bookingConfirmedQueue() {
		return new Queue(BOOKING_CONFIRMED_QUEUE, true);
	}

	@Bean
	public Queue paymentCompletedQueue() {
		return new Queue(PAYMENT_COMPLETED_QUEUE, true);
	}

	@Bean
	public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, DirectExchange eventsExchange) {
		return BindingBuilder.bind(bookingConfirmedQueue).to(eventsExchange).with(BOOKING_CONFIRMED_QUEUE);
	}

	@Bean
	public Binding paymentCompletedBinding(Queue paymentCompletedQueue, DirectExchange eventsExchange) {
		return BindingBuilder.bind(paymentCompletedQueue).to(eventsExchange).with(PAYMENT_COMPLETED_QUEUE);
	}
}

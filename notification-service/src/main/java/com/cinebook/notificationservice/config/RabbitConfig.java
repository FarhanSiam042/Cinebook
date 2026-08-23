package com.cinebook.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	public static final String EVENTS_EXCHANGE = "cinebook.events";
	public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed";

	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
		Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
		// Publishers (e.g. payment-service) stamp __TypeId__ with their own package's class name,
		// which doesn't exist on this service's classpath. Infer the target type from the
		// @RabbitListener method parameter instead of trusting that header.
		converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
		return converter;
	}

	@Bean
	public DirectExchange eventsExchange() {
		return new DirectExchange(EVENTS_EXCHANGE, true, false);
	}

	@Bean
	public Queue paymentCompletedQueue() {
		return new Queue(PAYMENT_COMPLETED_QUEUE, true);
	}

	@Bean
	public Binding paymentCompletedBinding(Queue paymentCompletedQueue, DirectExchange eventsExchange) {
		return BindingBuilder.bind(paymentCompletedQueue).to(eventsExchange).with(PAYMENT_COMPLETED_QUEUE);
	}
}

package com.cinebook.bookingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	public static final String EVENTS_EXCHANGE = "cinebook.events";
	public static final String BOOKING_CONFIRMED_QUEUE = "booking.confirmed";

	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	@Bean
	public DirectExchange eventsExchange() {
		return new DirectExchange(EVENTS_EXCHANGE, true, false);
	}
}

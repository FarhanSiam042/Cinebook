package com.cinebook.theaterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class TheaterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheaterServiceApplication.class, args);
	}
}

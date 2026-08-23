package com.cinebook.theaterservice.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cinebook.theaterservice.model.SeatCategory;
import com.cinebook.theaterservice.repository.SeatCategoryRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner seedSeatCategories(SeatCategoryRepository seatCategoryRepository) {
		return args -> {
			createCategoryIfMissing(seatCategoryRepository, "Silver", "SILVER", new BigDecimal("1.00"));
			createCategoryIfMissing(seatCategoryRepository, "Gold", "GOLD", new BigDecimal("1.25"));
			createCategoryIfMissing(seatCategoryRepository, "Premium", "PREMIUM", new BigDecimal("1.50"));
		};
	}

	private void createCategoryIfMissing(
		SeatCategoryRepository seatCategoryRepository,
		String name,
		String code,
		BigDecimal priceMultiplier
	) {
		if (seatCategoryRepository.existsByCodeIgnoreCase(code)) {
			return;
		}

		seatCategoryRepository.save(SeatCategory.builder()
			.name(name)
			.code(code)
			.priceMultiplier(priceMultiplier)
			.active(true)
			.build());
	}
}

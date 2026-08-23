package com.cinebook.userservice.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cinebook.userservice.model.Role;
import com.cinebook.userservice.model.User;
import com.cinebook.userservice.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.existsByUsername("admin")) {
				return;
			}

			userRepository.save(User.builder()
				.fullName("Default Admin")
				.email("admin@cinebook.local")
				.username("admin")
				.password(passwordEncoder.encode("Admin123!"))
				.roles(new HashSet<>(Set.of(Role.ADMIN)))
				.enabled(true)
				.build());
		};
	}
}

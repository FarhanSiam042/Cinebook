package com.cinebook.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cinebook.userservice.security.JpaUserDetailsService;
import com.cinebook.userservice.security.JwtAuthenticationFilter;

@Configuration
public class SecurityBeansConfig {

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider(JpaUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
	                                       JwtAuthenticationFilter jwtAuthenticationFilter,
	                                       DaoAuthenticationProvider authenticationProvider) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.cors(Customizer.withDefaults());
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authenticationProvider(authenticationProvider);
		http.authorizeHttpRequests(auth -> auth
			.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/actuator/health", "/actuator/info").permitAll()
			.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
			.anyRequest().authenticated());
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
package com.cinebook.notificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long paymentId;

	@Column(nullable = false)
	private Long bookingId;

	@Column(nullable = false, unique = true, length = 64)
	private String bookingReference;

	@Column(nullable = false)
	private String recipientEmail;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 64)
	private String transactionReference;

	@Column(nullable = false, length = 20)
	private String channel;

	@Column(nullable = false, length = 2000)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationStatus status;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime sentAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}

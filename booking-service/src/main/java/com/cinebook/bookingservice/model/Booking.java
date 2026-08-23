package com.cinebook.bookingservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String bookingReference;

	@Column(nullable = false)
	private String customerName;

	@Column(nullable = false)
	private String customerEmail;

	@Column(nullable = false)
	private Long movieId;

	@Column(nullable = false)
	private String movieTitle;

	@Column(nullable = false)
	private Long theaterId;

	@Column(nullable = false)
	private String theaterName;

	@Column(nullable = false)
	private LocalDateTime showTime;

	@Column(nullable = false)
	private Integer seatCount;

	// EAGER is deliberate: these lists are tiny (at most a few dozen seats) and need to be
	// readable by Jackson after the request's transaction/session has closed.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "booking_seats", joinColumns = @JoinColumn(name = "booking_id"))
	@OrderColumn(name = "seat_order")
	@Column(name = "seat_id", nullable = false)
	@Builder.Default
	private List<Long> seatIds = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "booking_seat_labels", joinColumns = @JoinColumn(name = "booking_id"))
	@OrderColumn(name = "seat_order")
	@Column(name = "seat_label", nullable = false)
	@Builder.Default
	private List<String> seatLabels = new ArrayList<>();

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BookingStatus status;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}

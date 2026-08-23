package com.cinebook.theaterservice.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seats", uniqueConstraints = {
	@UniqueConstraint(name = "uk_seats_screen_position", columnNames = {"screen_id", "row_label", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "screen_id", nullable = false)
	private Screen screen;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private SeatCategory category;

	@Column(name = "row_label", nullable = false)
	private String rowLabel;

	@Column(name = "seat_number", nullable = false)
	private Integer seatNumber;

	@Builder.Default
	@Column(nullable = false)
	private boolean active = true;

	@Column(nullable = false, updatable = false)
	@Builder.Default
	private Instant createdAt = Instant.now();

	@Column(nullable = false)
	@Builder.Default
	private Instant updatedAt = Instant.now();

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}

package com.cinebook.bookingservice.repository;

import com.cinebook.bookingservice.model.Booking;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	Optional<Booking> findByBookingReference(String bookingReference);
}

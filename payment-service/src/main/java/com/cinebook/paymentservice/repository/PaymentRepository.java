package com.cinebook.paymentservice.repository;

import com.cinebook.paymentservice.model.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByBookingReference(String bookingReference);
}

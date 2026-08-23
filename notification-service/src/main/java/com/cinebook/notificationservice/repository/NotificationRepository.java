package com.cinebook.notificationservice.repository;

import com.cinebook.notificationservice.model.Notification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Optional<Notification> findByBookingReference(String bookingReference);

	Optional<Notification> findByPaymentId(Long paymentId);
}

package com.cinebook.notificationservice.service;

import com.cinebook.notificationservice.dto.NotificationResponse;
import com.cinebook.notificationservice.dto.PaymentCompletedEvent;
import java.util.List;

public interface NotificationService {

	NotificationResponse processPaymentCompleted(PaymentCompletedEvent event);

	List<NotificationResponse> getAllNotifications();

	NotificationResponse getNotificationById(Long id);

	NotificationResponse getNotificationByBookingReference(String bookingReference);
}

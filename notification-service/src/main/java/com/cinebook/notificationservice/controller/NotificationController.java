package com.cinebook.notificationservice.controller;

import com.cinebook.notificationservice.dto.NotificationResponse;
import com.cinebook.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications", description = "Read operations for notifications sent via payment.completed events")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@Operation(summary = "Get all notifications")
	@GetMapping
	public List<NotificationResponse> getAllNotifications() {
		return notificationService.getAllNotifications();
	}

	@Operation(summary = "Get a notification by its ID")
	@GetMapping("/{id}")
	public NotificationResponse getNotificationById(@PathVariable Long id) {
		return notificationService.getNotificationById(id);
	}

	@Operation(summary = "Get a notification by booking reference")
	@GetMapping("/booking-reference/{bookingReference}")
	public NotificationResponse getNotificationByBookingReference(@PathVariable String bookingReference) {
		return notificationService.getNotificationByBookingReference(bookingReference);
	}
}

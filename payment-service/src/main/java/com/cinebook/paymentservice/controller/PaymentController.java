package com.cinebook.paymentservice.controller;

import com.cinebook.paymentservice.dto.PaymentResponse;
import com.cinebook.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Read operations for payments processed via booking.confirmed events")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get all payments")
    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @Operation(summary = "Get a payment by its ID")
    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @Operation(summary = "Get a payment by booking reference")
    @GetMapping("/booking-reference/{bookingReference}")
    public PaymentResponse getPaymentByBookingReference(@PathVariable String bookingReference) {
        return paymentService.getPaymentByBookingReference(bookingReference);
    }
}
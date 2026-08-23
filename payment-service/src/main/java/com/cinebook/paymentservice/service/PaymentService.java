package com.cinebook.paymentservice.service;

import com.cinebook.paymentservice.dto.BookingConfirmedEvent;
import com.cinebook.paymentservice.dto.PaymentResponse;
import java.util.List;

public interface PaymentService {

	PaymentResponse processBookingConfirmed(BookingConfirmedEvent event);

	List<PaymentResponse> getAllPayments();

	PaymentResponse getPaymentById(Long id);

	PaymentResponse getPaymentByBookingReference(String bookingReference);
}

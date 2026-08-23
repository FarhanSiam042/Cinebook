package com.cinebook.bookingservice.service;

import com.cinebook.bookingservice.dto.BookingResponse;
import com.cinebook.bookingservice.dto.CreateBookingRequest;
import java.util.List;

public interface BookingService {

	BookingResponse createBooking(CreateBookingRequest request);

	List<BookingResponse> getAllBookings();

	BookingResponse getBookingById(Long id);

	BookingResponse getBookingByReference(String bookingReference);
}

package com.cinebook.bookingservice.controller;

import com.cinebook.bookingservice.dto.BookingResponse;
import com.cinebook.bookingservice.dto.CreateBookingRequest;
import com.cinebook.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookings", description = "Create and retrieve movie ticket bookings")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking", description = "Looks up the showtime (and its movie/theater/screen) via Feign, saves the booking, and publishes a booking.confirmed event")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @Operation(summary = "Get all bookings")
    @GetMapping
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @Operation(summary = "Get a booking by its ID")
    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @Operation(summary = "Get a booking by its booking reference")
    @GetMapping("/reference/{bookingReference}")
    public BookingResponse getBookingByReference(@PathVariable String bookingReference) {
        return bookingService.getBookingByReference(bookingReference);
    }
}
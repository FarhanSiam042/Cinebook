package com.cinebook.seatlockservice.controller;

import com.cinebook.seatlockservice.dto.ConfirmHoldRequest;
import com.cinebook.seatlockservice.dto.ConfirmHoldResponse;
import com.cinebook.seatlockservice.dto.HoldSeatsRequest;
import com.cinebook.seatlockservice.dto.HoldSeatsResponse;
import com.cinebook.seatlockservice.dto.SeatStatusResponse;
import com.cinebook.seatlockservice.service.SeatLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seat-locks")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    @GetMapping("/showtimes/{showtimeId}")
    @Operation(summary = "Get held/booked seat status for a showtime — public", security = {})
    public ResponseEntity<List<SeatStatusResponse>> getSeatMap(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(seatLockService.getSeatMap(showtimeId));
    }

    @PostMapping
    @Operation(summary = "Hold seats for a showtime for a short window — must be logged in", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request, Authentication authentication) {
        return ResponseEntity.status(201).body(seatLockService.holdSeats(request, authentication.getName()));
    }

    @PostMapping("/{holdToken}/confirm")
    @Operation(summary = "Convert a hold into a permanent booking (called by booking-service)", security = {})
    public ResponseEntity<ConfirmHoldResponse> confirm(
            @PathVariable String holdToken, @Valid @RequestBody ConfirmHoldRequest request) {
        return ResponseEntity.ok(seatLockService.confirmHold(holdToken, request.bookingReference()));
    }

    @DeleteMapping("/{holdToken}")
    @Operation(summary = "Release a hold early", security = {})
    public ResponseEntity<Void> release(@PathVariable String holdToken) {
        seatLockService.releaseHold(holdToken);
        return ResponseEntity.noContent().build();
    }
}

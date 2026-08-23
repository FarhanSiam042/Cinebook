package com.cinebook.theaterservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinebook.theaterservice.dto.GenerateSeatsRequest;
import com.cinebook.theaterservice.dto.SeatResponse;
import com.cinebook.theaterservice.service.SeatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
@Tag(name = "Seats")
public class SeatController {

	private final SeatService seatService;

	@PostMapping("/screens/{screenId}/seats/generate")
	@Operation(summary = "Generate a seat grid for a screen")
	public ResponseEntity<List<SeatResponse>> generateSeats(
		@PathVariable Long screenId,
		@Valid @RequestBody GenerateSeatsRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(seatService.generateSeats(screenId, request));
	}

	@GetMapping("/screens/{screenId}/seats")
	@Operation(summary = "Get the seat map for a screen")
	public ResponseEntity<List<SeatResponse>> getSeatsForScreen(@PathVariable Long screenId) {
		return ResponseEntity.ok(seatService.getSeatsForScreen(screenId));
	}

	@GetMapping("/seats/{seatId}")
	@Operation(summary = "Get a seat by id")
	public ResponseEntity<SeatResponse> getSeat(@PathVariable Long seatId) {
		return ResponseEntity.ok(seatService.getSeat(seatId));
	}
}

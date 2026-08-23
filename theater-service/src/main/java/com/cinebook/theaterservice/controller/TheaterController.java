package com.cinebook.theaterservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinebook.theaterservice.dto.TheaterRequest;
import com.cinebook.theaterservice.dto.TheaterResponse;
import com.cinebook.theaterservice.service.TheaterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
@Tag(name = "Theaters")
public class TheaterController {

	private final TheaterService theaterService;

	@GetMapping
	@Operation(summary = "List all active theaters")
	public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
		return ResponseEntity.ok(theaterService.getAllTheaters());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a theater by id")
	public ResponseEntity<TheaterResponse> getTheater(@PathVariable Long id) {
		return ResponseEntity.ok(theaterService.getTheater(id));
	}

	@PostMapping
	@Operation(summary = "Create a theater")
	public ResponseEntity<TheaterResponse> createTheater(@Valid @RequestBody TheaterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createTheater(request));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a theater")
	public ResponseEntity<TheaterResponse> updateTheater(@PathVariable Long id, @Valid @RequestBody TheaterRequest request) {
		return ResponseEntity.ok(theaterService.updateTheater(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deactivate a theater")
	public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
		theaterService.deleteTheater(id);
		return ResponseEntity.noContent().build();
	}
}

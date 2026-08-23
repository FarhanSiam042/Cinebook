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

import com.cinebook.theaterservice.dto.ScreenRequest;
import com.cinebook.theaterservice.dto.ScreenResponse;
import com.cinebook.theaterservice.service.ScreenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
@Tag(name = "Screens")
public class ScreenController {

	private final ScreenService screenService;

	@GetMapping("/{theaterId}/screens")
	@Operation(summary = "List screens for a theater")
	public ResponseEntity<List<ScreenResponse>> getScreensForTheater(@PathVariable Long theaterId) {
		return ResponseEntity.ok(screenService.getScreensForTheater(theaterId));
	}

	@PostMapping("/{theaterId}/screens")
	@Operation(summary = "Create a screen for a theater")
	public ResponseEntity<ScreenResponse> createScreen(
		@PathVariable Long theaterId,
		@Valid @RequestBody ScreenRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(screenService.createScreen(theaterId, request));
	}

	@GetMapping("/screens/{screenId}")
	@Operation(summary = "Get a screen by id")
	public ResponseEntity<ScreenResponse> getScreen(@PathVariable Long screenId) {
		return ResponseEntity.ok(screenService.getScreen(screenId));
	}

	@PutMapping("/screens/{screenId}")
	@Operation(summary = "Update a screen")
	public ResponseEntity<ScreenResponse> updateScreen(
		@PathVariable Long screenId,
		@Valid @RequestBody ScreenRequest request
	) {
		return ResponseEntity.ok(screenService.updateScreen(screenId, request));
	}

	@DeleteMapping("/screens/{screenId}")
	@Operation(summary = "Deactivate a screen")
	public ResponseEntity<Void> deleteScreen(@PathVariable Long screenId) {
		screenService.deleteScreen(screenId);
		return ResponseEntity.noContent().build();
	}
}

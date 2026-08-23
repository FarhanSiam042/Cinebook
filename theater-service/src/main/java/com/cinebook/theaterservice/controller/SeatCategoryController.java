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

import com.cinebook.theaterservice.dto.SeatCategoryRequest;
import com.cinebook.theaterservice.dto.SeatCategoryResponse;
import com.cinebook.theaterservice.service.SeatCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/theaters/seat-categories")
@RequiredArgsConstructor
@Tag(name = "Seat Categories")
public class SeatCategoryController {

	private final SeatCategoryService seatCategoryService;

	@GetMapping
	@Operation(summary = "List active seat categories")
	public ResponseEntity<List<SeatCategoryResponse>> getAllCategories() {
		return ResponseEntity.ok(seatCategoryService.getAllCategories());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a seat category by id")
	public ResponseEntity<SeatCategoryResponse> getCategory(@PathVariable Long id) {
		return ResponseEntity.ok(seatCategoryService.getCategory(id));
	}

	@PostMapping
	@Operation(summary = "Create a seat category")
	public ResponseEntity<SeatCategoryResponse> createCategory(@Valid @RequestBody SeatCategoryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(seatCategoryService.createCategory(request));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a seat category")
	public ResponseEntity<SeatCategoryResponse> updateCategory(
		@PathVariable Long id,
		@Valid @RequestBody SeatCategoryRequest request
	) {
		return ResponseEntity.ok(seatCategoryService.updateCategory(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deactivate a seat category")
	public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
		seatCategoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}

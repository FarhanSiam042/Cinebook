package com.cinebook.theaterservice.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinebook.theaterservice.dto.SeatCategoryRequest;
import com.cinebook.theaterservice.dto.SeatCategoryResponse;
import com.cinebook.theaterservice.exception.InvalidOperationException;
import com.cinebook.theaterservice.exception.ResourceAlreadyExistsException;
import com.cinebook.theaterservice.exception.ResourceNotFoundException;
import com.cinebook.theaterservice.model.SeatCategory;
import com.cinebook.theaterservice.repository.SeatCategoryRepository;
import com.cinebook.theaterservice.repository.SeatRepository;
import com.cinebook.theaterservice.service.SeatCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatCategoryServiceImpl implements SeatCategoryService {

	private final SeatCategoryRepository seatCategoryRepository;
	private final SeatRepository seatRepository;

	@Override
	@Transactional(readOnly = true)
	public List<SeatCategoryResponse> getAllCategories() {
		return seatCategoryRepository.findByActiveTrueOrderByNameAsc().stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public SeatCategoryResponse getCategory(Long id) {
		return toResponse(findActiveCategory(id));
	}

	@Override
	public SeatCategoryResponse createCategory(SeatCategoryRequest request) {
		String code = normalizeCode(request.code());
		if (seatCategoryRepository.existsByCodeIgnoreCase(code)) {
			throw new ResourceAlreadyExistsException("Seat category code already exists");
		}

		SeatCategory category = SeatCategory.builder()
			.name(request.name().trim())
			.code(code)
			.priceMultiplier(request.priceMultiplier())
			.active(request.active() == null || request.active())
			.build();
		return toResponse(seatCategoryRepository.save(category));
	}

	@Override
	public SeatCategoryResponse updateCategory(Long id, SeatCategoryRequest request) {
		SeatCategory category = findActiveCategory(id);
		String code = normalizeCode(request.code());
		boolean changedCode = !category.getCode().equalsIgnoreCase(code);
		if (changedCode && seatCategoryRepository.existsByCodeIgnoreCase(code)) {
			throw new ResourceAlreadyExistsException("Seat category code already exists");
		}

		category.setName(request.name().trim());
		category.setCode(code);
		category.setPriceMultiplier(request.priceMultiplier());
		if (request.active() != null) {
			category.setActive(request.active());
		}
		return toResponse(seatCategoryRepository.save(category));
	}

	@Override
	public void deleteCategory(Long id) {
		SeatCategory category = findActiveCategory(id);
		if (seatRepository.existsByCategoryIdAndActiveTrue(id)) {
			throw new InvalidOperationException("Seat category is assigned to active seats");
		}
		category.setActive(false);
		seatCategoryRepository.save(category);
	}

	private SeatCategory findActiveCategory(Long id) {
		return seatCategoryRepository.findByIdAndActiveTrue(id)
			.orElseThrow(() -> new ResourceNotFoundException("Seat category not found"));
	}

	private String normalizeCode(String code) {
		return code.trim().toUpperCase(Locale.ROOT);
	}

	private SeatCategoryResponse toResponse(SeatCategory category) {
		return new SeatCategoryResponse(
			category.getId(),
			category.getName(),
			category.getCode(),
			category.getPriceMultiplier(),
			category.isActive()
		);
	}
}

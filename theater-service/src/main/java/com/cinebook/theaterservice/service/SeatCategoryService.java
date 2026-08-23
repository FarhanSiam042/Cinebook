package com.cinebook.theaterservice.service;

import java.util.List;

import com.cinebook.theaterservice.dto.SeatCategoryRequest;
import com.cinebook.theaterservice.dto.SeatCategoryResponse;

public interface SeatCategoryService {

	List<SeatCategoryResponse> getAllCategories();

	SeatCategoryResponse getCategory(Long id);

	SeatCategoryResponse createCategory(SeatCategoryRequest request);

	SeatCategoryResponse updateCategory(Long id, SeatCategoryRequest request);

	void deleteCategory(Long id);
}

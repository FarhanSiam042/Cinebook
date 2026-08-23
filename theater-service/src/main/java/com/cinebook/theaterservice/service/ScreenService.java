package com.cinebook.theaterservice.service;

import java.util.List;

import com.cinebook.theaterservice.dto.ScreenRequest;
import com.cinebook.theaterservice.dto.ScreenResponse;

public interface ScreenService {

	List<ScreenResponse> getScreensForTheater(Long theaterId);

	ScreenResponse getScreen(Long id);

	ScreenResponse createScreen(Long theaterId, ScreenRequest request);

	ScreenResponse updateScreen(Long id, ScreenRequest request);

	void deleteScreen(Long id);
}

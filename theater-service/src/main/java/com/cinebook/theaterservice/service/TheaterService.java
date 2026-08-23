package com.cinebook.theaterservice.service;

import java.util.List;

import com.cinebook.theaterservice.dto.TheaterRequest;
import com.cinebook.theaterservice.dto.TheaterResponse;

public interface TheaterService {

	List<TheaterResponse> getAllTheaters();

	TheaterResponse getTheater(Long id);

	TheaterResponse createTheater(TheaterRequest request);

	TheaterResponse updateTheater(Long id, TheaterRequest request);

	void deleteTheater(Long id);
}

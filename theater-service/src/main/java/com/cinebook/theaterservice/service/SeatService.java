package com.cinebook.theaterservice.service;

import java.util.List;

import com.cinebook.theaterservice.dto.GenerateSeatsRequest;
import com.cinebook.theaterservice.dto.SeatResponse;

public interface SeatService {

	List<SeatResponse> getSeatsForScreen(Long screenId);

	SeatResponse getSeat(Long id);

	List<SeatResponse> generateSeats(Long screenId, GenerateSeatsRequest request);
}

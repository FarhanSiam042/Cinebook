package com.cinebook.theaterservice.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinebook.theaterservice.dto.GenerateSeatsRequest;
import com.cinebook.theaterservice.dto.SeatResponse;
import com.cinebook.theaterservice.exception.InvalidOperationException;
import com.cinebook.theaterservice.exception.ResourceNotFoundException;
import com.cinebook.theaterservice.model.Screen;
import com.cinebook.theaterservice.model.Seat;
import com.cinebook.theaterservice.model.SeatCategory;
import com.cinebook.theaterservice.repository.ScreenRepository;
import com.cinebook.theaterservice.repository.SeatCategoryRepository;
import com.cinebook.theaterservice.repository.SeatRepository;
import com.cinebook.theaterservice.service.SeatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {

	private final SeatRepository seatRepository;
	private final ScreenRepository screenRepository;
	private final SeatCategoryRepository seatCategoryRepository;

	@Override
	@Transactional(readOnly = true)
	public List<SeatResponse> getSeatsForScreen(Long screenId) {
		if (screenRepository.findByIdAndActiveTrue(screenId).isEmpty()) {
			throw new ResourceNotFoundException("Screen not found");
		}
		return seatRepository.findByScreenIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(screenId).stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public SeatResponse getSeat(Long id) {
		return toResponse(seatRepository.findByIdAndActiveTrue(id)
			.orElseThrow(() -> new ResourceNotFoundException("Seat not found")));
	}

	@Override
	public List<SeatResponse> generateSeats(Long screenId, GenerateSeatsRequest request) {
		Screen screen = screenRepository.findByIdAndActiveTrue(screenId)
			.orElseThrow(() -> new ResourceNotFoundException("Screen not found"));
		if (seatRepository.existsByScreenId(screenId)) {
			throw new InvalidOperationException("Seats have already been generated for this screen");
		}

		SeatCategory category = seatCategoryRepository.findByCodeIgnoreCaseAndActiveTrue(request.categoryCode().trim())
			.orElseThrow(() -> new ResourceNotFoundException("Seat category not found"));

		List<Seat> seats = new ArrayList<>();
		for (int row = 0; row < screen.getTotalRows(); row++) {
			String rowLabel = String.valueOf((char) ('A' + row));
			for (int number = 1; number <= screen.getSeatsPerRow(); number++) {
				seats.add(Seat.builder()
					.screen(screen)
					.category(category)
					.rowLabel(rowLabel)
					.seatNumber(number)
					.active(true)
					.build());
			}
		}

		return seatRepository.saveAll(seats).stream()
			.map(this::toResponse)
			.toList();
	}

	private SeatResponse toResponse(Seat seat) {
		return new SeatResponse(
			seat.getId(),
			seat.getScreen().getTheater().getId(),
			seat.getScreen().getTheater().getName(),
			seat.getScreen().getId(),
			seat.getScreen().getName(),
			seat.getRowLabel(),
			seat.getSeatNumber(),
			seat.getCategory().getId(),
			seat.getCategory().getName(),
			seat.getCategory().getCode(),
			seat.getCategory().getPriceMultiplier(),
			seat.isActive()
		);
	}
}

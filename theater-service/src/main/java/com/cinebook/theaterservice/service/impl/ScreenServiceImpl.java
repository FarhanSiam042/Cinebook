package com.cinebook.theaterservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinebook.theaterservice.dto.ScreenRequest;
import com.cinebook.theaterservice.dto.ScreenResponse;
import com.cinebook.theaterservice.exception.ResourceAlreadyExistsException;
import com.cinebook.theaterservice.exception.ResourceNotFoundException;
import com.cinebook.theaterservice.model.Screen;
import com.cinebook.theaterservice.model.Theater;
import com.cinebook.theaterservice.repository.ScreenRepository;
import com.cinebook.theaterservice.repository.TheaterRepository;
import com.cinebook.theaterservice.service.ScreenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreenServiceImpl implements ScreenService {

	private final ScreenRepository screenRepository;
	private final TheaterRepository theaterRepository;

	@Override
	@Transactional(readOnly = true)
	public List<ScreenResponse> getScreensForTheater(Long theaterId) {
		if (theaterRepository.findByIdAndActiveTrue(theaterId).isEmpty()) {
			throw new ResourceNotFoundException("Theater not found");
		}
		return screenRepository.findByTheaterIdAndActiveTrueOrderByNameAsc(theaterId).stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ScreenResponse getScreen(Long id) {
		return toResponse(findActiveScreen(id));
	}

	@Override
	public ScreenResponse createScreen(Long theaterId, ScreenRequest request) {
		Theater theater = theaterRepository.findByIdAndActiveTrue(theaterId)
			.orElseThrow(() -> new ResourceNotFoundException("Theater not found"));
		String name = request.name().trim();
		if (screenRepository.existsByTheaterIdAndNameIgnoreCase(theaterId, name)) {
			throw new ResourceAlreadyExistsException("Screen already exists for this theater");
		}

		Screen screen = Screen.builder()
			.theater(theater)
			.name(name)
			.totalRows(request.totalRows())
			.seatsPerRow(request.seatsPerRow())
			.active(request.active() == null || request.active())
			.build();
		return toResponse(screenRepository.save(screen));
	}

	@Override
	public ScreenResponse updateScreen(Long id, ScreenRequest request) {
		Screen screen = findActiveScreen(id);
		String name = request.name().trim();
		boolean changedName = !screen.getName().equalsIgnoreCase(name);
		if (changedName && screenRepository.existsByTheaterIdAndNameIgnoreCase(screen.getTheater().getId(), name)) {
			throw new ResourceAlreadyExistsException("Screen already exists for this theater");
		}

		screen.setName(name);
		screen.setTotalRows(request.totalRows());
		screen.setSeatsPerRow(request.seatsPerRow());
		if (request.active() != null) {
			screen.setActive(request.active());
		}
		return toResponse(screenRepository.save(screen));
	}

	@Override
	public void deleteScreen(Long id) {
		Screen screen = findActiveScreen(id);
		screen.setActive(false);
		screenRepository.save(screen);
	}

	private Screen findActiveScreen(Long id) {
		return screenRepository.findByIdAndActiveTrue(id)
			.orElseThrow(() -> new ResourceNotFoundException("Screen not found"));
	}

	private ScreenResponse toResponse(Screen screen) {
		return new ScreenResponse(
			screen.getId(),
			screen.getTheater().getId(),
			screen.getTheater().getName(),
			screen.getName(),
			screen.getTotalRows(),
			screen.getSeatsPerRow(),
			screen.isActive()
		);
	}
}

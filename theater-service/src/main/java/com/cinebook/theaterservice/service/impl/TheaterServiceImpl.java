package com.cinebook.theaterservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinebook.theaterservice.dto.TheaterRequest;
import com.cinebook.theaterservice.dto.TheaterResponse;
import com.cinebook.theaterservice.exception.ResourceAlreadyExistsException;
import com.cinebook.theaterservice.exception.ResourceNotFoundException;
import com.cinebook.theaterservice.model.Theater;
import com.cinebook.theaterservice.repository.SeatRepository;
import com.cinebook.theaterservice.repository.TheaterRepository;
import com.cinebook.theaterservice.service.TheaterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TheaterServiceImpl implements TheaterService {

	private final TheaterRepository theaterRepository;
	private final SeatRepository seatRepository;

	@Override
	@Transactional(readOnly = true)
	public List<TheaterResponse> getAllTheaters() {
		return theaterRepository.findByActiveTrueOrderByCityAscNameAsc().stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public TheaterResponse getTheater(Long id) {
		return toResponse(findActiveTheater(id));
	}

	@Override
	public TheaterResponse createTheater(TheaterRequest request) {
		String name = request.name().trim();
		String city = request.city().trim();
		if (theaterRepository.existsByNameIgnoreCaseAndCityIgnoreCase(name, city)) {
			throw new ResourceAlreadyExistsException("Theater already exists in this city");
		}

		Theater theater = Theater.builder()
			.name(name)
			.city(city)
			.address(request.address().trim())
			.active(request.active() == null || request.active())
			.build();
		return toResponse(theaterRepository.save(theater));
	}

	@Override
	public TheaterResponse updateTheater(Long id, TheaterRequest request) {
		Theater theater = findActiveTheater(id);
		String name = request.name().trim();
		String city = request.city().trim();
		boolean changedIdentity = !theater.getName().equalsIgnoreCase(name) || !theater.getCity().equalsIgnoreCase(city);
		if (changedIdentity && theaterRepository.existsByNameIgnoreCaseAndCityIgnoreCase(name, city)) {
			throw new ResourceAlreadyExistsException("Theater already exists in this city");
		}

		theater.setName(name);
		theater.setCity(city);
		theater.setAddress(request.address().trim());
		if (request.active() != null) {
			theater.setActive(request.active());
		}
		return toResponse(theaterRepository.save(theater));
	}

	@Override
	public void deleteTheater(Long id) {
		Theater theater = findActiveTheater(id);
		theater.setActive(false);
		theaterRepository.save(theater);
	}

	private Theater findActiveTheater(Long id) {
		return theaterRepository.findByIdAndActiveTrue(id)
			.orElseThrow(() -> new ResourceNotFoundException("Theater not found"));
	}

	private TheaterResponse toResponse(Theater theater) {
		return new TheaterResponse(
			theater.getId(),
			theater.getName(),
			theater.getCity(),
			theater.getAddress(),
			theater.isActive(),
			(int) seatRepository.countActiveSeatsByTheaterId(theater.getId())
		);
	}
}

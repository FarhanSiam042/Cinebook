package com.cinebook.theaterservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinebook.theaterservice.model.Screen;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

	List<Screen> findByTheaterIdAndActiveTrueOrderByNameAsc(Long theaterId);

	Optional<Screen> findByIdAndActiveTrue(Long id);

	boolean existsByTheaterIdAndNameIgnoreCase(Long theaterId, String name);
}

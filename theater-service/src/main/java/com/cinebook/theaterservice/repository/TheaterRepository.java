package com.cinebook.theaterservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinebook.theaterservice.model.Theater;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

	List<Theater> findByActiveTrueOrderByCityAscNameAsc();

	Optional<Theater> findByIdAndActiveTrue(Long id);

	boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);
}

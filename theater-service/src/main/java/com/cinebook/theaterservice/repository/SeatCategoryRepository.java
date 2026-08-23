package com.cinebook.theaterservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinebook.theaterservice.model.SeatCategory;

public interface SeatCategoryRepository extends JpaRepository<SeatCategory, Long> {

	List<SeatCategory> findByActiveTrueOrderByNameAsc();

	Optional<SeatCategory> findByIdAndActiveTrue(Long id);

	Optional<SeatCategory> findByCodeIgnoreCaseAndActiveTrue(String code);

	boolean existsByCodeIgnoreCase(String code);
}

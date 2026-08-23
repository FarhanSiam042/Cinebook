package com.cinebook.theaterservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cinebook.theaterservice.model.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByScreenIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(Long screenId);

	Optional<Seat> findByIdAndActiveTrue(Long id);

	boolean existsByScreenId(Long screenId);

	boolean existsByCategoryIdAndActiveTrue(Long categoryId);

	@Query("select count(s) from Seat s where s.screen.theater.id = :theaterId "
		+ "and s.active = true and s.screen.active = true")
	long countActiveSeatsByTheaterId(@Param("theaterId") Long theaterId);
}

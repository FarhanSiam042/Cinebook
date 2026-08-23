package com.cinebook.movieservice.repository;

import com.cinebook.movieservice.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovie_Id(Long movieId);
}

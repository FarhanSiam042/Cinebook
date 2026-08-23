package com.cinebook.movieservice.repository;

import com.cinebook.movieservice.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByGenres_NameIgnoreCase(String genreName);
}
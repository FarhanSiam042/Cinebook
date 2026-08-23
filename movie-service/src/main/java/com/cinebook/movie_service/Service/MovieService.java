package com.cinebook.movieservice.service;

import com.cinebook.movieservice.dto.CastMemberDto;
import com.cinebook.movieservice.dto.MovieRequest;
import com.cinebook.movieservice.dto.MovieResponse;
import com.cinebook.movieservice.dto.ShowtimeDto;

import java.util.List;

public interface MovieService {

    MovieResponse create(MovieRequest request);

    MovieResponse getById(Long id);

    List<MovieResponse> getAll(String genre, String title);

    MovieResponse update(Long id, MovieRequest request);

    void delete(Long id);

    ShowtimeDto addShowtime(Long movieId, ShowtimeDto showtimeDto);

    List<ShowtimeDto> getShowtimes(Long movieId);

    ShowtimeDto getShowtimeById(Long showtimeId);

    CastMemberDto addCastMember(Long movieId, CastMemberDto castDto);
}
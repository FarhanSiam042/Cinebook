package com.cinebook.movieservice.service.impl;

import com.cinebook.movieservice.dto.CastMemberDto;
import com.cinebook.movieservice.dto.MovieRequest;
import com.cinebook.movieservice.dto.MovieResponse;
import com.cinebook.movieservice.dto.ShowtimeDto;
import com.cinebook.movieservice.exception.MovieNotFoundException;
import com.cinebook.movieservice.model.CastMember;
import com.cinebook.movieservice.model.Genre;
import com.cinebook.movieservice.model.Movie;
import com.cinebook.movieservice.model.Showtime;
import com.cinebook.movieservice.repository.GenreRepository;
import com.cinebook.movieservice.repository.MovieRepository;
import com.cinebook.movieservice.repository.ShowtimeRepository;
import com.cinebook.movieservice.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .language(request.getLanguage())
                .releaseDate(request.getReleaseDate())
                .rating(request.getRating())
                .posterUrl(request.getPosterUrl())
                .ticketPrice(request.getTicketPrice())
                .genres(resolveGenres(request.getGenres()))
                .build();

        return toResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getById(Long id) {
        return toResponse(findMovieOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAll(String genre, String title) {
        List<Movie> movies;
        if (genre != null && !genre.isBlank()) {
            movies = movieRepository.findByGenres_NameIgnoreCase(genre);
        } else if (title != null && !title.isBlank()) {
            movies = movieRepository.findByTitleContainingIgnoreCase(title);
        } else {
            movies = movieRepository.findAll();
        }
        return movies.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = findMovieOrThrow(id);
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setLanguage(request.getLanguage());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setRating(request.getRating());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTicketPrice(request.getTicketPrice());
        movie.setGenres(resolveGenres(request.getGenres()));
        return toResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movie movie = findMovieOrThrow(id);
        movieRepository.delete(movie);
    }

    @Override
    @Transactional
    public ShowtimeDto addShowtime(Long movieId, ShowtimeDto dto) {
        Movie movie = findMovieOrThrow(movieId);
        Showtime showtime = Showtime.builder()
                .startTime(dto.getStartTime())
                .theaterId(dto.getTheaterId())
                .screenId(dto.getScreenId())
                .price(dto.getPrice())
                .build();
        movie.addShowtime(showtime);
        movieRepository.save(movie);
        // fetch the persisted showtime (now with an id) to return
        Showtime saved = movie.getShowtimes().get(movie.getShowtimes().size() - 1);
        return toShowtimeDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDto> getShowtimes(Long movieId) {
        findMovieOrThrow(movieId); // validates existence
        return showtimeRepository.findByMovie_Id(movieId).stream()
                .map(this::toShowtimeDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShowtimeDto getShowtimeById(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new MovieNotFoundException("Showtime not found with id: " + showtimeId));
        return toShowtimeDto(showtime);
    }

    @Override
    @Transactional
    public CastMemberDto addCastMember(Long movieId, CastMemberDto dto) {
        Movie movie = findMovieOrThrow(movieId);
        CastMember member = CastMember.builder()
                .actorName(dto.getActorName())
                .roleInMovie(dto.getRoleInMovie())
                .build();
        movie.addCastMember(member);
        movieRepository.save(movie);
        CastMember saved = movie.getCast().get(movie.getCast().size() - 1);
        return toCastDto(saved);
    }

    // ---------- helpers ----------

    private Movie findMovieOrThrow(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    // resolve genre names to entities, creating any that don't exist yet
    private Set<Genre> resolveGenres(Set<String> genreNames) {
        if (genreNames == null) return Set.of();
        return genreNames.stream()
                .map(name -> genreRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> genreRepository.save(
                                Genre.builder().name(name).build())))
                .collect(Collectors.toSet());
    }

    private MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .language(movie.getLanguage())
                .releaseDate(movie.getReleaseDate())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .ticketPrice(movie.getTicketPrice())
                .genres(movie.getGenres().stream()
                        .map(Genre::getName).collect(Collectors.toSet()))
                .showtimes(movie.getShowtimes().stream()
                        .map(this::toShowtimeDto).collect(Collectors.toList()))
                .cast(movie.getCast().stream()
                        .map(this::toCastDto).collect(Collectors.toList()))
                .build();
    }

    private ShowtimeDto toShowtimeDto(Showtime s) {
        return ShowtimeDto.builder()
                .id(s.getId())
                .movieId(s.getMovie().getId())
                .startTime(s.getStartTime())
                .theaterId(s.getTheaterId())
                .screenId(s.getScreenId())
                .price(s.getPrice())
                .build();
    }

    private CastMemberDto toCastDto(CastMember c) {
        return CastMemberDto.builder()
                .id(c.getId())
                .actorName(c.getActorName())
                .roleInMovie(c.getRoleInMovie())
                .build();
    }
}
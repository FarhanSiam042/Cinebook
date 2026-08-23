package com.cinebook.movieservice.controller;

import com.cinebook.movieservice.dto.CastMemberDto;
import com.cinebook.movieservice.dto.MovieRequest;
import com.cinebook.movieservice.dto.MovieResponse;
import com.cinebook.movieservice.dto.ShowtimeDto;
import com.cinebook.movieservice.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Service", description = "Movie catalog: movies, genres, cast and showtimes")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @Operation(summary = "Create a new movie")
    public ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a movie by id")
    public MovieResponse getById(@PathVariable Long id) {
        return movieService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List movies, optionally filtered by genre or title")
    public List<MovieResponse> getAll(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title) {
        return movieService.getAll(genre, title);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing movie")
    public MovieResponse update(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return movieService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/showtimes")
    @Operation(summary = "Add a showtime to a movie")
    public ResponseEntity<ShowtimeDto> addShowtime(
            @PathVariable Long id, @RequestBody ShowtimeDto showtimeDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.addShowtime(id, showtimeDto));
    }

    @GetMapping("/{id}/showtimes")
    @Operation(summary = "Get all showtimes for a movie")
    public List<ShowtimeDto> getShowtimes(@PathVariable Long id) {
        return movieService.getShowtimes(id);
    }

    @GetMapping("/showtimes/{showtimeId}")
    @Operation(summary = "Get a single showtime by id")
    public ShowtimeDto getShowtimeById(@PathVariable Long showtimeId) {
        return movieService.getShowtimeById(showtimeId);
    }

    @PostMapping("/{id}/cast")
    @Operation(summary = "Add a cast member to a movie")
    public ResponseEntity<CastMemberDto> addCast(
            @PathVariable Long id, @RequestBody CastMemberDto castDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.addCastMember(id, castDto));
    }
}
package com.cinebook.bookingservice.feign;

import com.cinebook.bookingservice.dto.MovieInfoResponse;
import com.cinebook.bookingservice.dto.ShowtimeInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service")
public interface MovieClient {

	@GetMapping("/api/movies/{movieId}")
	MovieInfoResponse getMovieById(@PathVariable("movieId") Long movieId);

	@GetMapping("/api/movies/showtimes/{showtimeId}")
	ShowtimeInfoResponse getShowtimeById(@PathVariable("showtimeId") Long showtimeId);
}

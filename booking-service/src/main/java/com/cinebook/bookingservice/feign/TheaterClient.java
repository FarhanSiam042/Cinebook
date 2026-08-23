package com.cinebook.bookingservice.feign;

import com.cinebook.bookingservice.dto.ScreenInfoResponse;
import com.cinebook.bookingservice.dto.TheaterInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "theater-service")
public interface TheaterClient {

	@GetMapping("/api/theaters/{theaterId}")
	TheaterInfoResponse getTheaterById(@PathVariable("theaterId") Long theaterId);

	@GetMapping("/api/theaters/screens/{screenId}")
	ScreenInfoResponse getScreenById(@PathVariable("screenId") Long screenId);
}

package com.cinebook.bookingservice.feign;

import com.cinebook.bookingservice.dto.ConfirmHoldRequest;
import com.cinebook.bookingservice.dto.ConfirmHoldResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "seat-lock-service")
public interface SeatLockClient {

	@PostMapping("/api/seat-locks/{holdToken}/confirm")
	ConfirmHoldResponse confirm(@PathVariable("holdToken") String holdToken, @RequestBody ConfirmHoldRequest request);
}

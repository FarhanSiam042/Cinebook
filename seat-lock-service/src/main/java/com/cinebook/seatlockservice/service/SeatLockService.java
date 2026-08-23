package com.cinebook.seatlockservice.service;

import com.cinebook.seatlockservice.dto.ConfirmHoldResponse;
import com.cinebook.seatlockservice.dto.HoldSeatsRequest;
import com.cinebook.seatlockservice.dto.HoldSeatsResponse;
import com.cinebook.seatlockservice.dto.SeatStatusResponse;
import java.util.List;

public interface SeatLockService {

    List<SeatStatusResponse> getSeatMap(Long showtimeId);

    HoldSeatsResponse holdSeats(HoldSeatsRequest request, String username);

    ConfirmHoldResponse confirmHold(String holdToken, String bookingReference);

    void releaseHold(String holdToken);
}

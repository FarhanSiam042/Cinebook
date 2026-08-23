package com.cinebook.seatlockservice.dto;

import java.time.Instant;
import java.util.List;

public record HoldSeatsResponse(
        String holdToken,
        Long showtimeId,
        List<Long> seatIds,
        Instant expiresAt
) {
}

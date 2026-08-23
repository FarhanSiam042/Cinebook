package com.cinebook.seatlockservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HoldSeatsRequest(
        @NotNull Long showtimeId,
        @NotEmpty List<Long> seatIds
) {
}

package com.cinebook.seatlockservice.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class SeatUnavailableException extends RuntimeException {
    private final List<Long> conflictingSeatIds;

    public SeatUnavailableException(List<Long> conflictingSeatIds) {
        super("Seat(s) already held or booked: " + conflictingSeatIds);
        this.conflictingSeatIds = conflictingSeatIds;
    }
}

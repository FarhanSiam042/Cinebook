package com.cinebook.seatlockservice.service;

import java.util.List;

// what a "hold:{holdToken}" Redis key's value holds -- lets confirm/release find the seat keys by token alone.
public record HoldRecord(Long showtimeId, List<Long> seatIds, String username) {
}

package com.cinebook.seatlockservice.service;

// what a "seat:{showtimeId}:{seatId}" Redis key's value holds. status is "HELD" (has a TTL) or "BOOKED" (permanent).
public record SeatLockValue(String status, String holdToken, String bookingReference) {
}

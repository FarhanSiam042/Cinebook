package com.cinebook.seatlockservice.service.impl;

import com.cinebook.seatlockservice.config.SeatLockProperties;
import com.cinebook.seatlockservice.dto.ConfirmHoldResponse;
import com.cinebook.seatlockservice.dto.HoldSeatsRequest;
import com.cinebook.seatlockservice.dto.HoldSeatsResponse;
import com.cinebook.seatlockservice.dto.SeatStatusResponse;
import com.cinebook.seatlockservice.exception.HoldNotFoundException;
import com.cinebook.seatlockservice.exception.SeatUnavailableException;
import com.cinebook.seatlockservice.service.HoldRecord;
import com.cinebook.seatlockservice.service.SeatLockService;
import com.cinebook.seatlockservice.service.SeatLockValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatLockServiceImpl implements SeatLockService {

    private final StringRedisTemplate redisTemplate;
    private final SeatLockProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public List<SeatStatusResponse> getSeatMap(Long showtimeId) {
        Set<String> keys = redisTemplate.keys(seatKeyPattern(showtimeId));
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return keys.stream()
                .map(key -> {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json == null) {
                        return null; // expired between the keys() scan and the read
                    }
                    Long seatId = Long.valueOf(key.substring(key.lastIndexOf(':') + 1));
                    return new SeatStatusResponse(seatId, readValue(json).status());
                })
                .filter(s -> s != null)
                .toList();
    }

    @Override
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request, String username) {
        String holdToken = UUID.randomUUID().toString();
        Duration ttl = Duration.ofSeconds(properties.holdTtlSeconds());
        List<String> acquiredKeys = new ArrayList<>();
        List<Long> conflicting = new ArrayList<>();

        for (Long seatId : request.seatIds()) {
            String key = seatKey(request.showtimeId(), seatId);
            String value = writeValue(new SeatLockValue("HELD", holdToken, null));
            boolean acquired = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
            if (acquired) {
                acquiredKeys.add(key);
            }
            else {
                conflicting.add(seatId);
            }
        }

        if (!conflicting.isEmpty()) {
            acquiredKeys.forEach(redisTemplate::delete);
            throw new SeatUnavailableException(conflicting);
        }

        String holdRecord = writeValue(new HoldRecord(request.showtimeId(), request.seatIds(), username));
        redisTemplate.opsForValue().set(holdKey(holdToken), holdRecord, ttl);

        return new HoldSeatsResponse(holdToken, request.showtimeId(), request.seatIds(), Instant.now().plus(ttl));
    }

    @Override
    public ConfirmHoldResponse confirmHold(String holdToken, String bookingReference) {
        HoldRecord hold = loadHold(holdToken);
        hold.seatIds().forEach(seatId -> {
            String key = seatKey(hold.showtimeId(), seatId);
            String value = writeValue(new SeatLockValue("BOOKED", null, bookingReference));
            redisTemplate.opsForValue().set(key, value); // no TTL -- a confirmed booking holds the seat indefinitely
        });
        redisTemplate.delete(holdKey(holdToken));
        return new ConfirmHoldResponse(holdToken, hold.seatIds(), bookingReference);
    }

    @Override
    public void releaseHold(String holdToken) {
        HoldRecord hold = loadHold(holdToken);
        hold.seatIds().forEach(seatId -> redisTemplate.delete(seatKey(hold.showtimeId(), seatId)));
        redisTemplate.delete(holdKey(holdToken));
    }

    private HoldRecord loadHold(String holdToken) {
        String json = redisTemplate.opsForValue().get(holdKey(holdToken));
        if (json == null) {
            throw new HoldNotFoundException("Hold not found or expired: " + holdToken);
        }
        return readHold(json);
    }

    private String seatKey(Long showtimeId, Long seatId) {
        return "seat:" + showtimeId + ":" + seatId;
    }

    private String seatKeyPattern(Long showtimeId) {
        return "seat:" + showtimeId + ":*";
    }

    private String holdKey(String holdToken) {
        return "hold:" + holdToken;
    }

    @SneakyThrows
    private String writeValue(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    private SeatLockValue readValue(String json) {
        return objectMapper.readValue(json, SeatLockValue.class);
    }

    @SneakyThrows
    private HoldRecord readHold(String json) {
        return objectMapper.readValue(json, HoldRecord.class);
    }
}

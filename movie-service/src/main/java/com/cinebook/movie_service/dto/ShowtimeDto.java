package com.cinebook.movieservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDto {

    private Long id;
    private Long movieId;
    private LocalDateTime startTime;
    private Long theaterId;
    private Long screenId;
    private Double price;
}
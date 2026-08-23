package com.cinebook.movieservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String language;
    private LocalDate releaseDate;
    private Double rating;
    private String posterUrl;

    private BigDecimal ticketPrice;

    @Builder.Default
    private Set<String> genres = new HashSet<>();

    @Builder.Default
    private List<ShowtimeDto> showtimes = new ArrayList<>();

    @Builder.Default
    private List<CastMemberDto> cast = new ArrayList<>();
}
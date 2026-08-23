package com.cinebook.movieservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @Positive(message = "durationMinutes must be positive")
    private Integer durationMinutes;

    private String language;

    private LocalDate releaseDate;

    private Double rating;

    private String posterUrl;

    @Positive(message = "ticketPrice must be positive")
    private BigDecimal ticketPrice;

    // genre names -- created on the fly if they don't exist yet
    @Builder.Default
    private Set<String> genres = new HashSet<>();
}
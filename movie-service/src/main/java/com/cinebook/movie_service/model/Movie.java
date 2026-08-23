package com.cinebook.movieservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer durationMinutes;

    private String language;

    private LocalDate releaseDate;

    private Double rating;

    private String posterUrl;

    private BigDecimal ticketPrice;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Showtime> showtimes = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CastMember> cast = new ArrayList<>();

    public void addShowtime(Showtime showtime) {
        showtimes.add(showtime);
        showtime.setMovie(this);
    }

    public void addCastMember(CastMember member) {
        cast.add(member);
        member.setMovie(this);
    }
}
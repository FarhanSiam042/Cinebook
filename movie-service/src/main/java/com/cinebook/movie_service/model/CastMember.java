package com.cinebook.movieservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cast_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // real actor's name
    private String actorName;

    // character they play in this movie
    private String roleInMovie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private Movie movie;
}
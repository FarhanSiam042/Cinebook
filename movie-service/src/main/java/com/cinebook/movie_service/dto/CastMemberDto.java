package com.cinebook.movieservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMemberDto {

    private Long id;
    private String actorName;
    private String roleInMovie;
}
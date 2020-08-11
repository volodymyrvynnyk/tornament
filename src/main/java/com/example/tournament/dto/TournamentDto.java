package com.example.tournament.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentDto {

    private Long id;

    private String title;

    private int numberOfParticipants;

    private int maxNumberOfParticipants;

    private int numberOfSingleEliminationMatches;
}

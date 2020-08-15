package com.example.tournament.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public class TournamentListDto {

    private List<TournamentDto> tournaments;
}

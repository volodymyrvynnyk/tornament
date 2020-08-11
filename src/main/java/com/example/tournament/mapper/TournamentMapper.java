package com.example.tournament.mapper;

import com.example.tournament.dto.TournamentDto;
import com.example.tournament.model.Tournament;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TournamentMapper {

    public TournamentDto tournamentToDto(Tournament tournament) {
        return TournamentDto.builder()
                .id(tournament.getId())
                .title(tournament.getTitle())
                .maxNumberOfParticipants(tournament.getMaxNumberOfParticipants())
                .numberOfSingleEliminationMatches(tournament.getNumberOfSingleEliminationMatches())
                .build();
    }

    public List<TournamentDto> tournamentListToDto(List<Tournament> tournamentList) {
        return tournamentList.stream()
                .map(this::tournamentToDto)
                .collect(Collectors.toList());
    }

}

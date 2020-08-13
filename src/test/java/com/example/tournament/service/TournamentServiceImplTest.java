package com.example.tournament.service;

import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.model.Tournament;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class TournamentServiceImplTest {

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateMatches() {
        Tournament tournament = Tournament.builder()
                .title("Tournament")
                .id(12l)
                .maxNumberOfParticipants(16)
                .numberOfSingleEliminationMatches(6)
                .build();
        List<ParticipantDto> participantDtos = new ArrayList<>();

        participantDtos.add(ParticipantDto.builder()
                .id(1l)
                .name("Vasya")
                .tournamentId(tournament.getId())
                .build());
        participantDtos.add(ParticipantDto.builder()
                .id(2l)
                .name("Ivan")
                .tournamentId(tournament.getId())
                .build());
        participantDtos.add(ParticipantDto.builder()
                .id(3l)
                .name("Roman")
                .tournamentId(tournament.getId())
                .build());
        participantDtos.add(ParticipantDto.builder()
                .id(4l)
                .name("Andriy")
                .tournamentId(tournament.getId())
                .build());
        participantDtos.add(ParticipantDto.builder()
                .id(5l)
                .name("Stepan")
                .tournamentId(tournament.getId())
                .build());
        participantDtos.add(ParticipantDto.builder()
                .id(6l)
                .name("Vitalik")
                .tournamentId(tournament.getId())
                .build());
        participantDtos.add(ParticipantDto.builder()
                .id(7l)
                .name("Vlad")
                .tournamentId(tournament.getId())
                .build());
//        participantDtos.add(ParticipantDto.builder()
//                .id(8l)
//                .name("Mykola")
//                .tournamentId(tournament.getId())
//                .build());
//        participantDtos.add(ParticipantDto.builder()
//                .id(9l)
//                .name("Vova")
//                .tournamentId(tournament.getId())
//                .build());
//        participantDtos.add(ParticipantDto.builder()
//                .id(10l)
//                .name("Igor")
//                .tournamentId(tournament.getId())
//                .build());

//        tournamentService.generateMatches(participantDtos, tournament);


    }
}

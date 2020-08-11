package com.example.tournament.service;

import com.example.tournament.dto.ParticipantDto;

import java.util.List;

public interface ParticipantService {

    List<ParticipantDto> findAllByTournamentId(Long tournamentId);

    int countByTournamentId(Long tournamentId);

    ParticipantDto findById(Long id);

    void create(String name, long tournamentId);

    void delete(Long id);

    void deleteAllByTournamentId(Long tournamentId);

}

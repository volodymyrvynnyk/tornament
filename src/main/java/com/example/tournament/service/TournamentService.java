package com.example.tournament.service;

import com.example.tournament.dto.ParticipantsAddForm;
import com.example.tournament.dto.ParticipantsRemoveForm;
import com.example.tournament.dto.TournamentCreateForm;
import com.example.tournament.dto.TournamentDto;
import com.example.tournament.dto.TournamentGrid;

import java.util.List;

public interface TournamentService {

    List<TournamentDto> findAll();

    TournamentDto findById(Long id);

    void create(TournamentCreateForm tournamentCreateForm);

    void delete(Long id);

    void addParticipants(Long tournamentId, ParticipantsAddForm participantsAddForm);

    void removeParticipants(Long tournamentId, ParticipantsRemoveForm participantsRemoveForm);

    TournamentGrid start();

}

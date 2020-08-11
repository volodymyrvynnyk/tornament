package com.example.tournament.service;

import com.example.tournament.dto.TournamentCreateForm;
import com.example.tournament.dto.TournamentGrid;
import com.example.tournament.util.OperationResult;

import java.util.List;

public interface TournamentService {

    void create(TournamentCreateForm tournamentCreateForm);

    TournamentGrid start();

    void addParticipants(Long tournamentId, List<Long> participantIdList);

    void removeParticipants(Long tournamentId, List<Long> participantIdList);
}

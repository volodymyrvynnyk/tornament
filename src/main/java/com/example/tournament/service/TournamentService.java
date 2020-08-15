package com.example.tournament.service;

import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.dto.response.TournamentResultDto;

import java.util.List;

public interface TournamentService {

    List<TournamentDto> findAll();

    TournamentDto findById(Long id);

    List<MatchDto> startTournament(Long id);

    TournamentDto create(TournamentCreateForm tournamentCreateForm);

    TournamentResultDto summarizeTournament(Long id);

    void delete(Long id);

}

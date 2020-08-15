package com.example.tournament.service;

import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchListDto;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.dto.response.TournamentListDto;
import com.example.tournament.dto.response.TournamentResultDto;

public interface TournamentService {

    TournamentListDto findAll();

    TournamentDto findById(Long id);

    MatchListDto startTournament(Long id);

    TournamentDto create(TournamentCreateForm tournamentCreateForm);

    TournamentResultDto summarizeTournament(Long id);

    void delete(Long id);

}

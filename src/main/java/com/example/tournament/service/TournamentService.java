package com.example.tournament.service;

import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.TournamentDto;

import java.util.List;

public interface TournamentService {

    List<TournamentDto> findAll();

    TournamentDto findById(Long id);

    List<MatchDto> start(Long id);

    TournamentDto create(TournamentCreateForm tournamentCreateForm);

    Long summarize(Long id);

    void delete(Long id);

}

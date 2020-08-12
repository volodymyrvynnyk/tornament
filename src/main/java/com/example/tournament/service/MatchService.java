package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.model.Match;

import java.util.List;

public interface MatchService {

    List<MatchDto> findAllByTournament(Long tournamentId);

    void saveAll(List<Match> matches);

    void start(Long id);

    void update(Long id, MatchUpdateForm matchUpdateForm);

    void deleteAllByTournamentId(Long tournamentId);

    void disqualifyParticipant(Long participantId);
}

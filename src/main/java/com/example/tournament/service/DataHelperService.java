package com.example.tournament.service;

import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;

public interface DataHelperService {

    Tournament findTournamentByIdOrThrowException(Long id);

    Participant findParticipantByIdOrThrowException(Long id);

    Match findMatchByIdOrThrowException(Long id);

}

package com.example.tournament.service;

import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.ParticipantRepository;
import com.example.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataHelperServiceImpl implements DataHelperService {

    private final TournamentRepository tournamentRepository;

    private final ParticipantRepository participantRepository;

    private final MatchRepository matchRepository;

    @Autowired
    public DataHelperServiceImpl(TournamentRepository tournamentRepository, ParticipantRepository participantRepository, MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.participantRepository = participantRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public Tournament findTournamentByIdOrThrowException(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ServiceException(String.format("Tournament with id %s not found", id)));
    }

    @Override
    public Participant findParticipantByIdOrThrowException(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new ServiceException(String.format("Participant with id %s not found", id)));
    }

    @Override
    public Match findMatchByIdOrThrowException(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ServiceException(String.format("Match with id %s not found", id)));
    }
}

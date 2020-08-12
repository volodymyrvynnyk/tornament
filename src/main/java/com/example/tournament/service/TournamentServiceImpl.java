package com.example.tournament.service;

import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.mapper.TournamentMapper;
import com.example.tournament.model.Match;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;

    private final ParticipantService participantService;

    private final MatchService matchService;

    private final DataHelperService dataHelperService;

    private final TournamentMapper tournamentMapper;

    @Autowired
    public TournamentServiceImpl(TournamentRepository tournamentRepository, ParticipantService participantService, MatchService matchService, DataHelperService dataHelperService, TournamentMapper tournamentMapper) {
        this.tournamentRepository = tournamentRepository;
        this.participantService = participantService;
        this.matchService = matchService;
        this.dataHelperService = dataHelperService;
        this.tournamentMapper = tournamentMapper;
    }


    @Override
    public List<TournamentDto> findAll() {

        List<Tournament> tournaments = tournamentRepository.findAll();
        List<TournamentDto> tournamentDtos = tournamentMapper.tournamentListToDto(tournaments);

        tournamentDtos.forEach(this::setNumberOfParticipants);

        return tournamentDtos;
    }

    @Override
    public TournamentDto findById(Long id) {

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(id);
        TournamentDto tournamentDto = tournamentMapper.tournamentToDto(tournament);
        setNumberOfParticipants(tournamentDto);
        return tournamentDto;
    }

    private void setNumberOfParticipants(TournamentDto tournamentDto) {
        tournamentDto.setNumberOfParticipants(participantService.countByTournamentId(tournamentDto.getId()));
    }

    @Override
    @Transactional
    public void addParticipants(Long tournamentId, ParticipantsAddForm participantsAddForm) {

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        int numberOfParticipants = participantService.countByTournamentId(tournamentId);

        if (numberOfParticipants + participantsAddForm.getNames().size() > tournament.getMaxNumberOfParticipants()) {
            throw new ServiceException(String.format(
                    "Tournament '%s' can't get these participators. Limit will be exceeded", tournament.getTitle()));
        }

        participantsAddForm.getNames().forEach(participantName -> {
            participantService.create(participantName, tournament.getId());
        });

    }

    @Override
    @Transactional
    public void removeParticipant(Long tournamentId, Long participantId) {

        matchService.disqualifyParticipant(participantId);
        participantService.findById(participantId);

    }

    @Override
    @Transactional
    public List<MatchDto> start(Long id) {

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(id);

        int participantsNumber = participantService.countByTournamentId(id);
        tournament.setNumberOfSingleEliminationMatches(participantsNumber - 1);

        tournamentRepository.save(tournament);

        List<ParticipantDto> participants = participantService.findAllByTournamentId(tournament.getId());
        List<Match> matches = generateMatches(participants, tournament);
        matchService.saveAll(matches);

        return matchService.findAllByTournament(tournament.getId());
    }

    public List<Match> generateMatches(List<ParticipantDto> participants, Tournament tournament) {

        Collections.shuffle(participants);

        List<Match> matches = new ArrayList<>(tournament.getNumberOfSingleEliminationMatches());

        char label = 'A';

        for (int i = 0; i < tournament.getNumberOfSingleEliminationMatches(); i++) {
            if (i < participants.size() / 2) {
                matches.add(Match.builder()
                        .tournamentId(tournament.getId())
                        .label(label++)
                        .firstParticipantId(participants.get(i * 2).getId())
                        .secondParticipantId(participants.get(i * 2 + 1).getId())
                        .build()
                );
            } else {
                if (participants.size() > i * 2) {
                    matches.add(Match.builder()
                            .tournamentId(tournament.getId())
                            .label(label++)
                            .firstParticipantId(participants.get(i * 2).getId())
                            .build()
                    );
                } else {
                    matches.add(Match.builder()
                            .tournamentId(tournament.getId())
                            .label(label++)
                            .build()
                    );
                }
            }

        }

        label = (char) ('A' + participants.size() / 2); //Second tour label

        for (Match match : matches) {

            final char ctemp = label;

            long numberOfMatchesFollowed = matches.stream().filter(m -> m.getNextMatchLabel() == ctemp).count();
            Match matchToFollow = matches.stream().filter(m -> m.getLabel() == ctemp).findFirst().get();

            if (Objects.nonNull(matchToFollow.getFirstParticipantId())) {
                numberOfMatchesFollowed++;
            }
            if (Objects.nonNull(matchToFollow.getSecondParticipantId())) {
                numberOfMatchesFollowed++;
            }
            if (numberOfMatchesFollowed == 2) {
                label++;
            }

            if (matches.size() - 1 != matches.indexOf(match)){
                match.setNextMatchLabel(label);
            }

        }

        return matches;

    }

    @Override
    public void create(TournamentCreateForm tournamentCreateForm) {

        Tournament tournament = tournamentFormToTournament(tournamentCreateForm);

        tournamentRepository.save(tournament);
    }

    private Tournament tournamentFormToTournament(TournamentCreateForm tournamentCreateForm) {

        if (tournamentCreateForm.getMaxNumberOfParticipants() % 8 != 0) {
            throw new ServiceException("Tournament's max number of participants must be multiples of 8");
        }

        return Tournament.builder()
                .title(tournamentCreateForm.getTitle())
                .maxNumberOfParticipants(tournamentCreateForm.getMaxNumberOfParticipants())
                .build();

    }

    @Override
    @Transactional
    public void delete(Long id) {

        if (!tournamentRepository.existsById(id)) {
            throw new ServiceException(String.format("Tournament with id %s not found", id));
        }

        participantService.deleteAllByTournamentId(id);
        matchService.deleteAllByTournamentId(id);
        tournamentRepository.deleteById(id);

    }

}

package com.example.tournament.service;

import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.util.mapper.TournamentMapper;
import com.example.tournament.util.validation.DataValidator;
import com.example.tournament.util.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;

    private final ParticipantService participantService;

    private final MatchService matchService;

    private final DataHelperService dataHelperService;

    private final TournamentMapper tournamentMapper;

    private final DataValidator dataValidator;

    @Autowired
    public TournamentServiceImpl(TournamentRepository tournamentRepository, ParticipantService participantService,
                                 MatchService matchService, DataHelperService dataHelperService,
                                 TournamentMapper tournamentMapper, DataValidator dataValidator) {
        this.tournamentRepository = tournamentRepository;
        this.participantService = participantService;
        this.matchService = matchService;
        this.dataHelperService = dataHelperService;
        this.tournamentMapper = tournamentMapper;
        this.dataValidator = dataValidator;
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
    public List<MatchDto> start(Long id) {

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(id);

        int participantsNumber = participantService.countByTournamentId(id);

        if (participantsNumber < 2) {
            throw new ServiceException("Tournament (id '%s') must contain at least 2 participants");
        }

        Tournament updatedTournament = tournament.toBuilder()
                .numberOfSingleEliminationMatches(participantsNumber - 1)
                .status(EventStatus.STARTED)
                .build();

        tournamentRepository.save(updatedTournament);

        List<Participant> participants = participantService.findAllByTournamentId(updatedTournament.getId());
        List<Match> matches = matchService.generateMatches(participants, tournament);
        matchService.saveAll(matches);

        return matchService.findAllByTournamentId(tournament.getId());
    }


    @Override
    public TournamentDto create(TournamentCreateForm tournamentCreateForm) {

        ValidationResult validationResult = dataValidator.validate(tournamentCreateForm);

        if (validationResult.isError()) {
            throw new ServiceException("Validation error: " + validationResult.getErrorMessage());
        }

        if (tournamentCreateForm.getMaxNumberOfParticipants() % 8 != 0) {
            throw new ServiceException("Tournament's max number of participants must be multiples of 8");
        }

        Tournament tournament = Tournament.builder()
                .title(tournamentCreateForm.getTitle())
                .maxNumberOfParticipants(tournamentCreateForm.getMaxNumberOfParticipants())
                .status(EventStatus.PENDING)
                .build();

        Tournament tournamentFromDb = tournamentRepository.save(tournament);

        return tournamentMapper.tournamentToDto(tournamentFromDb);
    }

    @Override
    public Long summarize(Long id) {

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(id);

        Match finalMatch = matchService.findFinalMatchByTournamentId(tournament.getId());

        if (!finalMatch.getStatus().equals(EventStatus.COMPLETED)) {
            throw new ServiceException(String.format("Final match of tournament (id '%s') has't finished", id));
        }

        Tournament finishedTournament = tournament.toBuilder()
                .status(EventStatus.COMPLETED)
                .build();

        tournamentRepository.save(finishedTournament);

        return finalMatch.getWinnerId();
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

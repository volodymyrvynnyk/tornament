package com.example.tournament.service;

import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.ParticipantRepository;
import com.example.tournament.util.mapper.ParticipantMapper;
import com.example.tournament.util.validation.DataValidator;
import com.example.tournament.util.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    private final MatchService matchService;

    private final DataHelperService dataHelperService;

    private final ParticipantMapper participantMapper;

    private final DataValidator dataValidator;


    @Autowired
    public ParticipantServiceImpl(ParticipantRepository participantRepository, MatchService matchService, DataHelperService dataHelperService,
                                  ParticipantMapper participantMapper, DataValidator dataValidator) {
        this.participantRepository = participantRepository;
        this.matchService = matchService;
        this.dataHelperService = dataHelperService;
        this.participantMapper = participantMapper;
        this.dataValidator = dataValidator;
    }


    @Override
    public List<ParticipantDto> findAllByTournamentIdDto(Long tournamentId) {

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        List<Participant> participants = participantRepository.findAllByTournamentId(tournament.getId());

        return participantMapper.participantListToDto(participants);
    }

    @Override
    public List<Participant> findAllByTournamentId(Long tournamentId) {

        return participantRepository.findAllByTournamentId(tournamentId);
    }

    @Override
    public int countByTournamentId(Long tournamentId) {

        return participantRepository.countByTournamentId(tournamentId);
    }


    @Override
    public ParticipantDto findById(Long tournamentId, Long participantId) {

        Participant participant = dataHelperService.findParticipantByIdOrThrowException(participantId);

        if (!participant.getTournamentId().equals(tournamentId)) {
            throw new ServiceException(String.format("Participant (id '%s') doesn't belong to Tournament (id '%s')",
                    participant.getId(), tournamentId));
        }

        return participantMapper.participantToDto(participant);
    }

    @Override
    public List<ParticipantDto> createAll(Long tournamentId, ParticipantsAddForm participantsAddForm) {

        ValidationResult validationResult = dataValidator.validate(participantsAddForm);

        if (new HashSet<>(participantsAddForm.getNames()).size() < participantsAddForm.getNames().size()) {
            throw new ServiceException("Participants list has duplicates");
        }

        if (validationResult.isError()) {
            throw new ServiceException("Validation error: " + validationResult.getErrorMessage());
        }

        Tournament tournament = dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        int numberOfParticipants = countByTournamentId(tournamentId);

        if (numberOfParticipants + participantsAddForm.getNames().size() > tournament.getMaxNumberOfParticipants()) {
            throw new ServiceException(String.format(
                    "Tournament (id '%s') can't get these participators. Limit will be exceeded", tournament.getId()));
        }

        List<Participant> participants = participantsAddForm.getNames().stream()
                .map(name -> {
                    if (participantRepository.existsByNameAndTournamentId(name, tournamentId)) {
                        throw new ServiceException(String.format("Participant with name '%s' already exists in this tournament", name));
                    }
                    return Participant.builder()
                            .name(name)
                            .tournamentId(tournamentId)
                            .build();
                }).collect(Collectors.toList());

        List<Participant> participantsFromDb = participantRepository.saveAll(participants);

        return participantMapper.participantListToDto(participantsFromDb);
    }

    @Override
    @Transactional
    public void delete(Long tournamentId, Long participantId) {

        dataHelperService.findTournamentByIdOrThrowException(tournamentId);
        Optional<Match> optionalMatch = matchService.findUncompletedMatchByParticipantId(tournamentId, participantId);

        optionalMatch.ifPresent(match -> matchService.disqualifyParticipantById(match, participantId));
        participantRepository.deleteByTournamentIdAndId(tournamentId, participantId);
    }

    @Override
    public void deleteAllByTournamentId(Long tournamentId) {

        participantRepository.deleteAllByTournamentId(tournamentId);
    }
}

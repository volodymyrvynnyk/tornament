package com.example.tournament.service;

import com.example.tournament.dto.ParticipantsAddForm;
import com.example.tournament.dto.ParticipantsRemoveForm;
import com.example.tournament.dto.TournamentCreateForm;
import com.example.tournament.dto.TournamentDto;
import com.example.tournament.dto.TournamentGrid;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.mapper.TournamentMapper;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;

    private final ParticipantService participantService;

    private final DataHelperService dataHelperService;

    private final TournamentMapper tournamentMapper;

    @Autowired
    public TournamentServiceImpl(TournamentRepository tournamentRepository, ParticipantService participantService, DataHelperService dataHelperService, TournamentMapper tournamentMapper) {
        this.tournamentRepository = tournamentRepository;
        this.participantService = participantService;
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
    public void removeParticipants(Long tournamentId, ParticipantsRemoveForm participantsRemoveForm) {

        participantsRemoveForm.getIdList().forEach(participantId -> {
            if (participantService.findById(participantId).getTournamentId().equals(tournamentId)) {
                participantService.delete(participantId);
            }
        });

    }

    @Override
    public void create(TournamentCreateForm tournamentCreateForm) {

        Tournament tournament = Tournament.builder()
                .title(tournamentCreateForm.getTitle())
                .maxNumberOfParticipants(tournamentCreateForm.getMaxNumberOfParticipants())
                .numberOfSingleEliminationMatches(tournamentCreateForm.getNumberOfSingleEliminationMatches())
                .build();

        tournamentRepository.save(tournament);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        if (!tournamentRepository.existsById(id)) {
            throw new ServiceException(String.format("Tournament with id %s not found", id));
        }

        participantService.deleteAllByTournamentId(id);
        tournamentRepository.deleteById(id);

    }

    @Override
    public TournamentGrid start() {
        return null;
    }
}

package com.example.tournament.service;

import com.example.tournament.dto.ParticipantDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.mapper.ParticipantMapper;
import com.example.tournament.model.Participant;
import com.example.tournament.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    private final DataHelperService dataHelperService;

    private final ParticipantMapper participantMapper;


    @Autowired
    public ParticipantServiceImpl(ParticipantRepository participantRepository, DataHelperService dataHelperService,
                                  ParticipantMapper participantMapper) {
        this.participantRepository = participantRepository;
        this.dataHelperService = dataHelperService;
        this.participantMapper = participantMapper;
    }


    @Override
    public List<ParticipantDto> findAllByTournamentId(Long tournamentId) {

        dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        List<Participant> participants = participantRepository.findAllByTournamentId(tournamentId);

        return participantMapper.participantListToDto(participants);
    }

    @Override
    public int countByTournamentId(Long tournamentId) {
        return participantRepository.countByTournamentId(tournamentId);
    }


    @Override
    public ParticipantDto findById(Long id) {

        Participant participant = dataHelperService.findParticipantByIdOrThrowException(id);

        return participantMapper.participantToDto(participant);
    }

    @Override
    public void create(String name, long tournamentId) {

        if (participantRepository.existsByNameAndTournamentId(name, tournamentId)) {
            throw new ServiceException(String.format("Participant with name '%s' already exists in this tournament", name));
        }

        participantRepository.save(Participant.builder()
                .name(name)
                .tournamentId(tournamentId)
                .build());
    }

    @Override
    public void delete(Long id) {

        participantRepository.deleteById(id);
    }

    @Override
    public void deleteAllByTournamentId(Long tournamentId) {
        participantRepository.deleteAllByTournamentId(tournamentId);
    }
}

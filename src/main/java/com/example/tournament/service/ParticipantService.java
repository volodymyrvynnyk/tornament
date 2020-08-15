package com.example.tournament.service;

import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.dto.response.ParticipantListDto;
import com.example.tournament.model.Participant;

import java.util.List;

public interface ParticipantService {

    ParticipantListDto findParticipantListByTournamentId(Long tournamentId);

    List<Participant> findAllByTournamentId(Long tournamentId);

    int countByTournamentId(Long tournamentId);

    ParticipantDto findById(Long tournamentId, Long participantId);

    ParticipantListDto createAll(Long tournamentId, ParticipantsAddForm participantsAddForm);

    void delete(Long tournamentId, Long participantId);

    void deleteAllByTournamentId(Long tournamentId);

}

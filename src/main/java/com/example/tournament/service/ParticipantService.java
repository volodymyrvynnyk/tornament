package com.example.tournament.service;

import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.model.Participant;

import java.util.List;

public interface ParticipantService {

    List<ParticipantDto> findAllByTournamentIdDto(Long tournamentId);

    List<Participant> findAllByTournamentId(Long tournamentId);

    int countByTournamentId(Long tournamentId);

    ParticipantDto findById(Long tournamentId, Long participantId);

    List<ParticipantDto> createAll(Long tournamentId, ParticipantsAddForm participantsAddForm);

    void delete(Long tournamentId, Long participantId);

    void deleteAllByTournamentId(Long tournamentId);

}

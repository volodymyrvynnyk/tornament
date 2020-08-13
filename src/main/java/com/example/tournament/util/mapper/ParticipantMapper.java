package com.example.tournament.util.mapper;

import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.model.Participant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParticipantMapper {

    public ParticipantDto participantToDto(Participant participant) {
        return ParticipantDto.builder()
                .id(participant.getId())
                .tournamentId(participant.getTournamentId())
                .name(participant.getName())
                .build();
    }

    public List<ParticipantDto> participantListToDto(List<Participant> participantList) {
        return participantList.stream()
                .map(this::participantToDto)
                .collect(Collectors.toList());
    }
}

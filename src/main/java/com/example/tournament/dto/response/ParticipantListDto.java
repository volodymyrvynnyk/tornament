package com.example.tournament.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ParticipantListDto {

    private List<ParticipantDto> participants;
}

package com.example.tournament.dto.response;

import com.example.tournament.model.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {

    private Long id;

    private char label;

    private char nextMatchLabel;

    private Long firstParticipantId;

    private Long secondParticipantId;

    private String firstParticipant;

    private String secondParticipant;

    private String score;

    private Long winnerId;

    private LocalTime start;

    private LocalTime finish;

    private EventStatus status;


}

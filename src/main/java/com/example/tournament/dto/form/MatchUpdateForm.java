package com.example.tournament.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchUpdateForm {

    @PositiveOrZero(message = "Score can't be negative")
    private int firstParticipantScore;

    @PositiveOrZero(message = "Score can't be negative")
    private int secondParticipantScore;

    private Long winnerId;
}

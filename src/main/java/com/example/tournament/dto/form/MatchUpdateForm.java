package com.example.tournament.dto.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchUpdateForm {

    private int firstParticipantScore;

    private int secondParticipantScore;

    private Long winnerId;
}

package com.example.tournament.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalTime;

@Data
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private char label;

    private Long tournamentId;

    private Long firstParticipantId;

    private int firstParticipantScore;

    private Long secondParticipantId;

    private int secondParticipantScore;

    private Long winnerId;

    private char nextMatchLabel;

    private LocalTime startTime;

    private LocalTime finishTime;

//    @Enumerated(EnumType.STRING)
//    private MatchStatus status;

}

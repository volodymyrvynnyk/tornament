package com.example.tournament.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tournamentId;

    private Long firstParticipantId;

    private int firstParticipantScore;

    private Long secondParticipantId;

    private int secondParticipantScore;

    private LocalTime startTime;

    private LocalTime finishTime;

}

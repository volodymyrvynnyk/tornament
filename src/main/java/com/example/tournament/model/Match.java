package com.example.tournament.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "matches")
public class Match {

    private Long id;

    private Long tournamentId;

    private Long firstParticipantId;

    private Long secondParticipantId;

    private int[] score = new int[2];

    private LocalTime startTime;

    private LocalTime finishTime;

}

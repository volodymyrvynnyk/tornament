package com.example.tournament.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "tournaments")
public class Tournament {

    private Long id;

    private int maxNumberOfParticipants;

    private int numberOfSingleEliminationMatches;
}

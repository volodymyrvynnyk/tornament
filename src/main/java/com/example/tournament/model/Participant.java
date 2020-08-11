package com.example.tournament.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "participants")
public class Participant {

    private Long id;

    private String name;
}

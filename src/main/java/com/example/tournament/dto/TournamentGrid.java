package com.example.tournament.dto;

import lombok.Data;
import org.springframework.data.util.Pair;

import java.util.List;

@Data
public class TournamentGrid {

    private List<Pair> pairs;
}

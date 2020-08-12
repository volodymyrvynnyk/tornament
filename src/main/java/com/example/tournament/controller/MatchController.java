package com.example.tournament.controller;


import com.example.tournament.service.MatchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/matches", produces = MediaType.APPLICATION_JSON_VALUE)
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public void findAllByTournamentId(@RequestParam(name = "tournamentId") Long tournamentId) {

        matchService.findAllByTournament(tournamentId);
    }

    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {

        matchService.start(id);
    }


}

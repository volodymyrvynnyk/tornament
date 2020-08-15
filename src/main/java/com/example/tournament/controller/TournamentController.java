package com.example.tournament.controller;


import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchListDto;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.dto.response.TournamentListDto;
import com.example.tournament.dto.response.TournamentResultDto;
import com.example.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
public class TournamentController {

    private final TournamentService tournamentService;

    @Autowired
    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public TournamentListDto findAll() {
        return tournamentService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TournamentDto findById(@PathVariable(name = "id") Long id) {
        return tournamentService.findById(id);
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentDto create(@RequestBody TournamentCreateForm tournamentCreateForm) {
        return tournamentService.create(tournamentCreateForm);
    }

    @PostMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tournamentService.delete(id);
    }

    @PostMapping("/{id}/start")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchListDto start(@PathVariable Long id) {
        return tournamentService.startTournament(id);
    }

    @PostMapping("/{id}/summarize")
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResultDto summarize(@PathVariable Long id) {
        return tournamentService.summarizeTournament(id);
    }
}

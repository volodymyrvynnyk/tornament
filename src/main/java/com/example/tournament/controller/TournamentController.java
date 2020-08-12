package com.example.tournament.controller;


import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.dto.form.ParticipantsRemoveForm;
import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<TournamentDto> findAll() {
        return tournamentService.findAll();
    }

    @GetMapping("/{id}")
    public TournamentDto findById(@PathVariable(name = "id") Long id) {
        return tournamentService.findById(id);
    }

    @PostMapping("/new")
    public void create(@RequestBody TournamentCreateForm tournamentCreateForm) {
        tournamentService.create(tournamentCreateForm);
    }

    @PostMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        tournamentService.delete(id);
    }

    @PostMapping("/{id}/add")
    public void addParticipants(@PathVariable Long id, @RequestBody ParticipantsAddForm participantsAddForm) {
        tournamentService.addParticipants(id, participantsAddForm);
    }

    @PostMapping("/{id}/remove")
    public void removeParticipants(@PathVariable Long id, @RequestBody ParticipantsRemoveForm participantsRemoveForm) {
        tournamentService.removeParticipant(id, participantsRemoveForm);
    }

    @PostMapping("{id}/start")
    public List<MatchDto> start(@PathVariable Long id) {
        return tournamentService.start(id);
    }
}

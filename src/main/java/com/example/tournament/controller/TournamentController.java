package com.example.tournament.controller;


import com.example.tournament.dto.ParticipantsAddForm;
import com.example.tournament.dto.ParticipantsRemoveForm;
import com.example.tournament.dto.TournamentCreateForm;
import com.example.tournament.dto.TournamentDto;
import com.example.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RepositoryRestController
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
        tournamentService.removeParticipants(id, participantsRemoveForm);
    }
}

package com.example.tournament.controller;

import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantController {

    private final ParticipantService participantService;

    @Autowired
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping
    public List<ParticipantDto> findAll(@RequestParam(name = "tournamentId") Long tournamentId) {
        return participantService.findAllByTournamentId(tournamentId);
    }

    @GetMapping("/{id}")
    public ParticipantDto findById(@PathVariable Long id) {
        return participantService.findById(id);
    }


}

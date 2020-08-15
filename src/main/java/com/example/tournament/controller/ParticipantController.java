package com.example.tournament.controller;

import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.service.ParticipantService;
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
@RequestMapping(value = "/tournaments/{tournamentId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantController {

    private final ParticipantService participantService;

    @Autowired
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipantDto> findAllByTournament(@PathVariable Long tournamentId) {
        return participantService.findAllByTournamentIdDto(tournamentId);
    }

    @GetMapping("/{participantId}")
    @ResponseStatus(HttpStatus.OK)
    public ParticipantDto findById(@PathVariable Long tournamentId, @PathVariable Long participantId) {
        return participantService.findById(tournamentId, participantId);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ParticipantDto> addParticipants(@PathVariable Long tournamentId, @RequestBody ParticipantsAddForm participantsAddForm) {
        return participantService.createAll(tournamentId, participantsAddForm);
    }


    @PostMapping("/remove/{participantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long tournamentId, @PathVariable Long participantId) {
        participantService.delete(tournamentId, participantId);
    }


}

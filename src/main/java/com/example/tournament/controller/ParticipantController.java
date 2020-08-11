package com.example.tournament.controller;

import com.example.tournament.dto.PageDto;
import com.example.tournament.dto.ParticipantCreateForm;
import com.example.tournament.dto.ParticipantDto;
import com.example.tournament.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RepositoryRestController
@RequestMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantController {

    private final ParticipantService participantService;

    @Autowired
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping
    public PageDto<ParticipantDto> findAll(@PageableDefault(size = 10) Pageable pageable) {
        return participantService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ParticipantDto findById(@PathVariable Long id) {
        return participantService.findById(id);
    }

    @PostMapping("/new")
    public void create(@RequestBody ParticipantCreateForm participantCreateForm) {
        participantService.create(participantCreateForm);
    }

    @PostMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        participantService.delete(id);
    }

}

package com.example.tournament.controller;


import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.service.MatchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/tournaments/{tournamentId}/matches", produces = MediaType.APPLICATION_JSON_VALUE)
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchDto> findAllByTournamentId(@PathVariable Long tournamentId) {

        return matchService.findAllByTournamentId(tournamentId);
    }

    @GetMapping("/{matchId}")
    public MatchDto findById(@PathVariable Long tournamentId, @PathVariable Long matchId) {

        return matchService.findById(tournamentId, matchId);
    }

    @PostMapping("/{matchId}/start")
    public MatchDto start(@PathVariable Long tournamentId, @PathVariable Long matchId) {

        return matchService.startMatch(tournamentId, matchId);
    }

    @PostMapping("/update/{matchId}")
    public MatchDto update(@PathVariable Long tournamentId, @PathVariable Long matchId,
                           @RequestBody MatchUpdateForm matchUpdateForm) {

        return matchService.updateMatch(tournamentId, matchId, matchUpdateForm);
    }

}

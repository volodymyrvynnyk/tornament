package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.mapper.MatchMapper;
import com.example.tournament.model.Match;
import com.example.tournament.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;

    private final MatchMapper matchMapper;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository, MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
    }

    @Override
    public List<MatchDto> findAllByTournament(Long tournamentId) {

        List<Match> matches = matchRepository.findAllByTournamentId(tournamentId);

        List<MatchDto> matchDtoList = matchMapper.matchListToDto(matches);

        matchDtoList.forEach(m -> {

            List<Match> matchesBeforeThisOne = matches.stream().
                    filter(match -> match.getNextMatchLabel() == m.getLabel())
                    .collect(Collectors.toList());

            if (isNull(m.getFirstParticipantId()) && isNull(m.getSecondParticipantId())) {
                m.setFirstParticipant(String.format("Winner of match '%s'", matchesBeforeThisOne.get(0).getLabel()));
                m.setSecondParticipant(String.format("Winner of match '%s'", matchesBeforeThisOne.get(1).getLabel()));
            } else {
                if (isNull(m.getFirstParticipantId())) {
                    m.setFirstParticipant(String.format("Winner of match '%s'", matchesBeforeThisOne.get(0).getLabel()));
                }
                if (isNull(m.getSecondParticipantId())) {
                    m.setSecondParticipant(String.format("Winner of match '%s'", matchesBeforeThisOne.get(0).getLabel()));
                }
            }
        });
        return matchDtoList;
    }

    @Override
    public void saveAll(List<Match> matches) {

        matchRepository.saveAll(matches);
    }


    @Override
    public void update(MatchUpdateForm matchUpdateForm) {

    }

    @Override
    public void deleteAllByTournamentId(Long tournamentId) {

        matchRepository.deleteAllByTournamentId(tournamentId);
    }
}

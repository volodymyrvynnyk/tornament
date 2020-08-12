package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.mapper.MatchMapper;
import com.example.tournament.model.Match;
import com.example.tournament.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;

    private final DataHelperService dataHelperService;

    private final MatchMapper matchMapper;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository, DataHelperService dataHelperService, MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.dataHelperService = dataHelperService;
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
    public void start(Long id) {

        Match matchFromDb = dataHelperService.findMatchByIdOrThrowException(id);

        if (isNull(matchFromDb.getFirstParticipantId()) || isNull(matchFromDb.getSecondParticipantId())) {
            throw new ServiceException(String.format("Match (id '%s') hasn't got 2 participants", matchFromDb.getId()));
        }

        Match updatedMatch = matchFromDb.toBuilder()
                .startTime(LocalTime.now())
                .firstParticipantScore(0)
                .secondParticipantScore(0)
                .build();

        matchRepository.save(updatedMatch);
    }


    @Override
    @Transactional
    public void update(Long id, MatchUpdateForm matchUpdateForm) {

        Match matchFromDb = dataHelperService.findMatchByIdOrThrowException(id);

        if (isNull(matchFromDb.getStartTime())) {
            throw new ServiceException(String.format("Match (id '%s') hasn't been started", id));
        }

        Match updatedMatch = matchFromDb.toBuilder()
                .firstParticipantScore(matchUpdateForm.getFirstParticipantScore())
                .secondParticipantScore(matchUpdateForm.getSecondParticipantScore())
                .build();

        if (!isNull(matchUpdateForm.getWinnerId())) {
            finish(updatedMatch, matchUpdateForm.getWinnerId());
        }

        matchRepository.save(updatedMatch);
    }

    private void finish(Match match, Long winnerId) {

        if (!winnerId.equals(match.getFirstParticipantId()) && !winnerId.equals(match.getSecondParticipantId())) {
            throw new ServiceException("Wrong winner id");
        }

        Match finishedMatch = match.toBuilder()
                .winnerId(winnerId)
                .finishTime(LocalTime.now())
                .build();

        List<Match> tournamentMatches = matchRepository.findAllByTournamentId(match.getTournamentId());
        Match nextMatch = tournamentMatches.stream()
                .filter(m -> m.getLabel() == finishedMatch.getNextMatchLabel()).findFirst().get();
        nextMatch.addParticipant(finishedMatch.getWinnerId());
        matchRepository.save(finishedMatch);
        matchRepository.save(nextMatch);
    }

    @Override
    public void deleteAllByTournamentId(Long tournamentId) {

        matchRepository.deleteAllByTournamentId(tournamentId);
    }

    @Override
    public void disqualifyParticipant(Long participantId) {

        Match match = matchRepository.findByFirstParticipantIdOrSecondParticipantIdAndFinishTimeIsNull(participantId,
                participantId)
                .orElseThrow(() -> new ServiceException(
                        String.format("Match with participant id = '%s' not found", participantId)));

        Long winnerId = match.getFirstParticipantId().equals(participantId) ?
                match.getSecondParticipantId() :
                match.getFirstParticipantId();

        finish(match, winnerId);
    }
}

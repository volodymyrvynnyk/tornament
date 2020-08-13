package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.ParticipantDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.util.mapper.MatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;

    private final DataHelperService dataHelperService;

    private final MatchMapper matchMapper;

    private static final String REF_TO_PREV_MATCH_MESSAGE = "Winner of match '%s'";

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
                m.setFirstParticipant(String.format(REF_TO_PREV_MATCH_MESSAGE, matchesBeforeThisOne.get(0).getLabel()));
                m.setSecondParticipant(String.format(REF_TO_PREV_MATCH_MESSAGE, matchesBeforeThisOne.get(1).getLabel()));
            } else {
                if (isNull(m.getFirstParticipantId())) {
                    m.setFirstParticipant(String.format(REF_TO_PREV_MATCH_MESSAGE, matchesBeforeThisOne.get(0).getLabel()));
                }
                if (isNull(m.getSecondParticipantId())) {
                    m.setSecondParticipant(String.format(REF_TO_PREV_MATCH_MESSAGE, matchesBeforeThisOne.get(0).getLabel()));
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
    public MatchDto start(Long tournamentId, Long id) {

        Match matchFromDb = dataHelperService.findMatchByIdOrThrowException(id);

        checkIfMatchBelongsToTournament(tournamentId, matchFromDb);

        if (isNull(matchFromDb.getFirstParticipantId()) || isNull(matchFromDb.getSecondParticipantId())) {
            throw new ServiceException(String.format("Match (id '%s') hasn't got 2 participants", matchFromDb.getId()));
        }

        Match updatedMatch = matchFromDb.toBuilder()
                .startTime(LocalTime.now())
                .firstParticipantScore(0)
                .secondParticipantScore(0)
                .status(EventStatus.STARTED)
                .build();

        matchRepository.save(updatedMatch);

        return matchMapper.matchToDto(updatedMatch);
    }


    @Override
    @Transactional
    public MatchDto update(Long tournamentId, Long matchId, MatchUpdateForm matchUpdateForm) {

        Match matchFromDb = dataHelperService.findMatchByIdOrThrowException(matchId);

        checkIfMatchBelongsToTournament(tournamentId, matchFromDb);

        if (!matchFromDb.getStatus().equals(EventStatus.STARTED)) {
            throw new ServiceException(String.format("Match (id '%s') hasn't been started", matchId));
        }

        Match updatedMatch = matchFromDb.toBuilder()
                .firstParticipantScore(matchUpdateForm.getFirstParticipantScore())
                .secondParticipantScore(matchUpdateForm.getSecondParticipantScore())
                .build();

        if (!isNull(matchUpdateForm.getWinnerId())) {
            finish(updatedMatch, matchUpdateForm.getWinnerId());
        }

        matchRepository.save(updatedMatch);

        return matchMapper.matchToDto(updatedMatch);
    }

    private void checkIfMatchBelongsToTournament(Long tournamentId, Match matchFromDb) {
        if (!matchFromDb.getTournamentId().equals(tournamentId)) {
            throw new ServiceException((String.format("Match (id '%s') doesn't belong to Tournament  (id '%s')",
                    matchFromDb.getId(), tournamentId)));
        }
    }

    private void finish(Match match, Long winnerId) {

        if (!winnerId.equals(match.getFirstParticipantId()) && !winnerId.equals(match.getSecondParticipantId())) {
            throw new ServiceException("Wrong winner id");
        }

        Match finishedMatch = match.toBuilder()
                .winnerId(winnerId)
                .finishTime(LocalTime.now())
                .status(EventStatus.COMPLETED)
                .build();

        List<Match> tournamentMatches = matchRepository.findAllByTournamentId(match.getTournamentId());
        if (nonNull(match.getNextMatchLabel())) {
            Match nextMatch = tournamentMatches.stream()
                    .filter(m -> m.getLabel() == finishedMatch.getNextMatchLabel()).findFirst().get();
            nextMatch.addParticipant(finishedMatch.getWinnerId());
            matchRepository.save(nextMatch);
        }
        matchRepository.save(finishedMatch);

    }

    @Override
    public void deleteAllByTournamentId(Long tournamentId) {

        matchRepository.deleteAllByTournamentId(tournamentId);
    }

    @Override
    public Optional<Match> findMatchByParticipant(Long tournamentId, Long participantId) {

        return matchRepository.findByFirstParticipantIdOrSecondParticipantIdAndStatus(participantId,
                participantId, EventStatus.COMPLETED);
    }

    @Override
    public void disqualifyParticipant(Match match, Long participantId) {

        Long winnerId = match.getFirstParticipantId().equals(participantId) ?
                match.getSecondParticipantId() :
                match.getFirstParticipantId();

        finish(match, winnerId);
    }

    @Override
    public Match findFinalMatchByTournamentId(Long tournamentId) {

        return matchRepository.findByNextMatchLabelIsNullAndTournamentId(tournamentId).orElseThrow(() ->
                new ServiceException(String.format("Final match for tournament (id '%s') not found", tournamentId)));
    }

    @Override
    public List<Match> generateMatches(List<ParticipantDto> participants, Tournament tournament) {

        Collections.shuffle(participants);

        List<Match> matches = new ArrayList<>(tournament.getNumberOfSingleEliminationMatches());

        char label = 'A';

        for (int i = 0; i < tournament.getNumberOfSingleEliminationMatches(); i++) {
            if (i < participants.size() / 2) {
                matches.add(Match.builder()
                        .tournamentId(tournament.getId())
                        .label(label++)
                        .firstParticipantId(participants.get(i * 2).getId())
                        .secondParticipantId(participants.get(i * 2 + 1).getId())
                        .status(EventStatus.PENDING)
                        .build()
                );
            } else {
                if (participants.size() > i * 2) {
                    matches.add(Match.builder()
                            .tournamentId(tournament.getId())
                            .label(label++)
                            .firstParticipantId(participants.get(i * 2).getId())
                            .status(EventStatus.PENDING)
                            .build()
                    );
                } else {
                    matches.add(Match.builder()
                            .tournamentId(tournament.getId())
                            .status(EventStatus.PENDING)
                            .label(label++)
                            .build()
                    );
                }
            }

        }

        label = (char) ('A' + participants.size() / 2); //Second tour label

        for (Match match : matches) {

            final char ctemp = label;

            long numberOfMatchesFollowed = matches.stream().filter(m -> m.getNextMatchLabel() == ctemp).count();
            Match matchToFollow = matches.stream().filter(m -> m.getLabel() == ctemp).findFirst().get();

            if (Objects.nonNull(matchToFollow.getFirstParticipantId())) {
                numberOfMatchesFollowed++;
            }
            if (Objects.nonNull(matchToFollow.getSecondParticipantId())) {
                numberOfMatchesFollowed++;
            }
            if (numberOfMatchesFollowed == 2) {
                label++;
            }

            if (matches.size() - 1 != matches.indexOf(match)) {
                match.setNextMatchLabel(label);
            }

        }

        return matches;

    }
}

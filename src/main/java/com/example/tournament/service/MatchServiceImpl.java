package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.dto.response.MatchListDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.util.mapper.MatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    public MatchListDto findMatchListByTournamentId(Long tournamentId) {

        dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        List<Match> matches = matchRepository.findAllByTournamentId(tournamentId);

        List<MatchDto> matchDtos = matchMapper.matchListToDto(matches);

        matchDtos.forEach(m -> {

            List<Character> previousMatchLabels = matchDtos.stream()
                    .filter(match -> !isNull(match.getNextMatchLabel()))
                    .filter(match -> match.getNextMatchLabel().equals(m.getLabel()))
                    .map(MatchDto::getLabel)
                    .collect(Collectors.toList());

            m.setPreviousMatchLabels(previousMatchLabels);

        });
        return MatchListDto.builder()
                .matches(matchDtos)
                .build();
    }


    @Override
    public MatchDto findById(Long tournamentId, Long matchId) {

        dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        Match match = dataHelperService.findMatchByIdOrThrowException(matchId);
        checkIfMatchBelongsToTournament(tournamentId, match);
        return matchMapper.matchToDto(match);
    }

    @Override
    public MatchDto startMatch(Long tournamentId, Long matchId) {

        Match matchFromDb = dataHelperService.findMatchByIdOrThrowException(matchId);

        if (!matchFromDb.getStatus().equals(EventStatus.PENDING)) {
            throw new ServiceException(String.format("Match (id '%s') has been already started", matchFromDb.getId()));
        }

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
    public MatchDto updateMatch(Long tournamentId, Long matchId, MatchUpdateForm matchUpdateForm) {

        Match matchFromDb = dataHelperService.findMatchByIdOrThrowException(matchId);

        if (matchFromDb.getStatus().equals(EventStatus.PENDING)) {
            throw new ServiceException(String.format("Match (id '%s') hasn't been started", matchId));
        }

        if (matchFromDb.getStatus().equals(EventStatus.COMPLETED)) {
            throw new ServiceException(String.format("Match (id '%s') has been already finished", matchId));
        }

        checkIfMatchBelongsToTournament(tournamentId, matchFromDb);

        Match updatedMatch = matchFromDb.toBuilder()
                .firstParticipantScore(matchUpdateForm.getFirstParticipantScore())
                .secondParticipantScore(matchUpdateForm.getSecondParticipantScore())
                .build();

        if (matchUpdateForm.isFinished()) {

            Long winnerId = updatedMatch.getFirstParticipantScore() > updatedMatch.getSecondParticipantScore() ?
                    updatedMatch.getFirstParticipantId() : updatedMatch.getSecondParticipantId();

            updatedMatch = finishMatch(updatedMatch, winnerId);
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

    private Match finishMatch(Match match, Long winnerId) {

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

        return finishedMatch;

    }

    @Override
    public void deleteAllByTournamentId(Long tournamentId) {

        matchRepository.deleteAllByTournamentId(tournamentId);
    }

    @Override
    public Optional<Match> findUncompletedMatchByParticipantId(Long tournamentId, Long participantId) {

        List<Match> matchList = matchRepository.findByFirstParticipantIdOrSecondParticipantId(participantId,
                participantId).stream()
                .filter(match -> !match.getStatus().equals(EventStatus.COMPLETED))
                .collect(Collectors.toList());

        if (matchList.size() > 1) {
            throw new ServiceException(String.format("Participant (id '%s') belongs to more than one uncompleted matches"));
        }

        if (matchList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(matchList.get(0));
    }

    @Override
    public void disqualifyParticipantById(Match match, Long participantId) {

        Long winnerId = match.getFirstParticipantId().equals(participantId) ?
                match.getSecondParticipantId() :
                match.getFirstParticipantId();

        Match finishedMatch = finishMatch(match, winnerId);
        matchRepository.save(finishedMatch);
    }

    @Override
    public Match findFinalMatchByTournamentId(Long tournamentId) {

        return matchRepository.findByNextMatchLabelIsNullAndTournamentId(tournamentId).orElseThrow(() ->
                new ServiceException(String.format("Final match for tournament (id '%s') not found", tournamentId)));
    }

    @Override
    public List<Match> generateMatches(List<Participant> participants, Tournament tournament) {

        Collections.shuffle(participants);

        List<Match> matches = new ArrayList<>(tournament.getNumberOfSingleEliminationMatches());
        Queue<Participant> participantQueue = new ArrayDeque<>(participants);

        char label = 'A';
        for (int i = 0; i < tournament.getNumberOfSingleEliminationMatches(); i++) {

            Match match = Match.builder()
                    .tournamentId(tournament.getId())
                    .label(label++)
                    .status(EventStatus.PENDING)
                    .build();

            if (!participantQueue.isEmpty()) {
                match.addParticipant(participantQueue.poll().getId());
            }
            if (!participantQueue.isEmpty()) {
                match.addParticipant(participantQueue.poll().getId());
            }
            matches.add(match);

        }

        char nextMatchLabel = (char) ('A' + participants.size() / 2); //Second tour first match label

        for (Match match : matches) {

            final Character nextMatchLabelFinal = nextMatchLabel;
            long numberOfParticipants = matches.stream()
                    .filter(m -> nonNull(m.getNextMatchLabel()) && m.getNextMatchLabel().equals(nextMatchLabelFinal))
                    .count();

            numberOfParticipants += matches.stream()
                    .filter(m -> m.getLabel().equals(nextMatchLabelFinal))
                    .findFirst()
                    .orElse(Match.builder().build())
                    .getNumberOfParticipants();

            if (numberOfParticipants == 2) {
                nextMatchLabel++;
            }

            if (matches.size() - 1 > matches.indexOf(match)) { //Avoid setting nextMatchLabel to final match
                match.setNextMatchLabel(nextMatchLabel);
            }
        }

        return matchRepository.saveAll(matches);
    }
}

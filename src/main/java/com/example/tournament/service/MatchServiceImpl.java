package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.util.mapper.MatchMapper;
import com.example.tournament.util.validation.DataValidator;
import com.example.tournament.util.validation.ValidationResult;
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

    private final DataValidator dataValidator;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository, DataHelperService dataHelperService, MatchMapper matchMapper, DataValidator dataValidator) {
        this.matchRepository = matchRepository;
        this.dataHelperService = dataHelperService;
        this.matchMapper = matchMapper;
        this.dataValidator = dataValidator;
    }

    @Override
    public List<MatchDto> findAllByTournamentId(Long tournamentId) {

        List<Match> matches = matchRepository.findAllByTournamentId(tournamentId);

        List<MatchDto> matchDtoList = matchMapper.matchListToDto(matches);

        matchDtoList.forEach(m -> {

            List<Character> previousMatchLabels = matches.stream()
                    .filter(match -> !isNull(match.getNextMatchLabel()))
                    .filter(match -> match.getNextMatchLabel().equals(m.getLabel()))
                    .map(Match::getLabel)
                    .collect(Collectors.toList());

            m.setPreviousMatchLabels(previousMatchLabels);

        });
        return matchDtoList;
    }

    @Override
    public MatchDto findById(Long tournamentId, Long matchId) {

        dataHelperService.findTournamentByIdOrThrowException(tournamentId);

        Match match = dataHelperService.findMatchByIdOrThrowException(matchId);
        checkIfMatchBelongsToTournament(tournamentId, match);
        return matchMapper.matchToDto(match);
    }

    @Override
    public void saveAll(List<Match> matches) {

        matchRepository.saveAll(matches);
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

        ValidationResult validationResult = dataValidator.validate(matchUpdateForm);

        if (validationResult.isError()) {
            throw new ServiceException("Validation error: " + validationResult.getErrorMessage());
        }

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

            long numberOfPotentialParticipants = matches.stream().filter(m -> nonNull(m.getNextMatchLabel()) && m.getNextMatchLabel() == ctemp).count();

            Optional<Match> optionalMatchToFollow = matches.stream().filter(m -> m.getLabel() == ctemp).findFirst();

            if (optionalMatchToFollow.isPresent()) {
                Match matchToFollow = optionalMatchToFollow.get();
                numberOfPotentialParticipants += Objects.nonNull(matchToFollow.getFirstParticipantId()) ? 1 : 0;
                numberOfPotentialParticipants += Objects.nonNull(matchToFollow.getSecondParticipantId()) ? 1 : 0;
                label += numberOfPotentialParticipants == 2 ? 1 : 0;

                if (matches.size() - 1 != matches.indexOf(match)) {
                    match.setNextMatchLabel(label);
                }
            }
        }

        return matchRepository.saveAll(matches);
    }
}

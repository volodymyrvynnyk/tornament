package com.example.tournament.service;

import com.example.tournament.dto.form.MatchUpdateForm;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.util.mapper.MatchMapper;
import com.example.tournament.util.validation.DataValidator;
import com.example.tournament.util.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MatchServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private DataHelperService dataHelperService;

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private DataValidator dataValidator;

    @Captor
    private ArgumentCaptor<List<Match>> matchListArgumentCaptor;

    @InjectMocks
    private MatchServiceImpl matchService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findAllByTournamentTest() {

        Long tournamentId = 1l;
        matchService.findAllByTournamentId(tournamentId);

        verify(matchRepository, times(1)).findAllByTournamentId(tournamentId);
    }

    @Test
    public void saveAllTest() {

        List<Match> matchList = new ArrayList<>();
        matchService.saveAll(matchList);

        verify(matchRepository, times(1)).saveAll(matchList);
    }

    @Test
    public void startTest_ExceptionFlow() {

        Long tournamentId = 1l;
        Long matchId = 1l;
        Match match = Match.builder()
                .id(matchId)
                .tournamentId(tournamentId)
                .status(EventStatus.PENDING)
                .build();

        when(dataHelperService.findMatchByIdOrThrowException(matchId))
                .thenReturn(match);

        Exception exception = assertThrows(ServiceException.class, () -> {
            matchService.startMatch(tournamentId, matchId);
        });
        String expectedMessage = String.format("Match (id '%s') hasn't got 2 participants", match.getId());
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(dataHelperService, times(1)).findMatchByIdOrThrowException(matchId);
    }

    @Test
    public void startTest_SuccessFlow() {

        Long tournamentId = 1l;
        Long matchId = 1l;

        Match match = Match.builder()
                .id(matchId)
                .tournamentId(tournamentId)
                .firstParticipantId(1l)
                .secondParticipantId(2l)
                .status(EventStatus.PENDING)
                .build();

        when(dataHelperService.findMatchByIdOrThrowException(matchId))
                .thenReturn(match);

        matchService.startMatch(tournamentId, matchId);

        verify(dataHelperService, times(1)).findMatchByIdOrThrowException(matchId);
        verify(matchRepository, times((1))).save(any(Match.class));
    }

    @Test
    public void updateTest_StillNoWinnerFlow() {

        Long tournamentId = 1l;
        Long matchId = 1l;

        MatchUpdateForm matchUpdateForm = MatchUpdateForm.builder()
                .firstParticipantScore(1)
                .secondParticipantScore(0)
                .build();

        Match matchFromDb = Match.builder()
                .id(matchId)
                .tournamentId(tournamentId)
                .firstParticipantId(1l)
                .secondParticipantId(2l)
                .status(EventStatus.STARTED)
                .build();

        when(dataValidator.validate(matchUpdateForm)).thenReturn(ValidationResult.valid());

        when(dataHelperService.findMatchByIdOrThrowException(matchId))
                .thenReturn(matchFromDb);

        matchService.updateMatch(tournamentId, matchId, matchUpdateForm);

        verify(dataHelperService, times(1)).findMatchByIdOrThrowException(matchId);
        verify(matchRepository, times((1))).save(any(Match.class));
    }

    @Test
    public void updateTest_GetWinnerFlow() {

        Long tournamentId = 1l;
        Long matchId = 1l;
        Character nextMatchLabel = 'C';

        MatchUpdateForm matchUpdateForm = MatchUpdateForm.builder()
                .firstParticipantScore(2)
                .secondParticipantScore(3)
                .finished(true)
                .build();

        Match matchFromDb = Match.builder()
                .id(matchId)
                .tournamentId(tournamentId)
                .firstParticipantId(1l)
                .secondParticipantId(2l)
                .nextMatchLabel(nextMatchLabel)
                .status(EventStatus.STARTED)
                .build();

        when(dataValidator.validate(matchUpdateForm)).thenReturn(ValidationResult.valid());

        when(matchRepository.findAllByTournamentId(tournamentId))
                .thenReturn(Arrays.asList(Match.builder()
                        .label(nextMatchLabel)
                        .build()));

        when(dataHelperService.findMatchByIdOrThrowException(matchId))
                .thenReturn(matchFromDb);

        matchService.updateMatch(tournamentId, matchId, matchUpdateForm);

        verify(dataHelperService, times(1)).findMatchByIdOrThrowException(matchId);
        verify(matchRepository, times((2))).save(any(Match.class));
        verify(matchRepository, times(1)).findAllByTournamentId(tournamentId);
    }

    @Test
    public void deleteAllByTournamentId() {

        Long tournamentId = 1l;
        matchService.deleteAllByTournamentId(tournamentId);
        verify(matchRepository, times(1)).deleteAllByTournamentId(tournamentId);
    }

    @Test
    public void disqualifyParticipantById() {

        Long participantToDisqualifyId = 1l;
        Long winnerId = 2l;

        Match match = Match.builder()
                .firstParticipantId(participantToDisqualifyId)
                .secondParticipantId(winnerId)
                .build();

        matchService.disqualifyParticipantById(match, participantToDisqualifyId);
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    public void generateMatchesTest() {

        List<Participant> participants = Arrays.asList(
                Participant.builder().id(1l).build(),
                Participant.builder().id(2l).build(),
                Participant.builder().id(3l).build(),
                Participant.builder().id(4l).build(),
                Participant.builder().id(5l).build(),
                Participant.builder().id(6l).build(),
                Participant.builder().id(7l).build()
        );

        Tournament tournament = Tournament.builder()
                .id(1l)
                .numberOfSingleEliminationMatches(6)
                .maxNumberOfParticipants(8)
                .build();

        List<Match> expectedMatches = Arrays.asList(
                Match.builder().label('A').nextMatchLabel('D').build(),
                Match.builder().label('B').nextMatchLabel('E').build(),
                Match.builder().label('C').nextMatchLabel('E').build(),
                Match.builder().label('D').nextMatchLabel('F').build(),
                Match.builder().label('E').nextMatchLabel('F').build(),
                Match.builder().label('F').build()
        );

        when(matchRepository.saveAll(matchListArgumentCaptor.capture())).thenReturn(new ArrayList<>());

        matchService.generateMatches(participants, tournament);

        List<Match> matches = matchListArgumentCaptor.getValue();
        matches.sort(Comparator.comparingInt(Match::getLabel));

        for (int i = 0; i < tournament.getNumberOfSingleEliminationMatches(); i++) {
            assertEquals(expectedMatches.get(i).getLabel(), matches.get(i).getLabel());
            assertEquals(expectedMatches.get(i).getNextMatchLabel(), matches.get(i).getNextMatchLabel());
        }

    }
}

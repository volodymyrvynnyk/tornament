package com.example.tournament.service;

import com.example.tournament.dto.form.ParticipantsAddForm;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.Match;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.ParticipantRepository;
import com.example.tournament.util.mapper.ParticipantMapper;
import com.example.tournament.util.validation.DataValidator;
import com.example.tournament.util.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ParticipantServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private DataHelperService dataHelperService;

    @Mock
    private ParticipantMapper participantMapper;

    @Mock
    private DataValidator dataValidator;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findAllByTournamentIdDtoTest() {

        Long tournamentId = 1l;
        when(dataHelperService.findTournamentByIdOrThrowException(tournamentId))
                .thenReturn(Tournament.builder()
                        .id(tournamentId)
                        .build());

        participantService.findParticipantListByTournamentId(tournamentId);
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournamentId);
        verify(participantRepository, times(1)).findAllByTournamentId(tournamentId);

    }

    @Test
    public void countByTournamentIdTest() {

        Long tournamentId = 1l;
        participantService.countByTournamentId(tournamentId);
        verify(participantRepository, times(1)).countByTournamentId(tournamentId);
    }

    @Test
    public void findById_SuccessFlow() {

        Long tournamentId = 1l;
        Long participantId = 1l;

        when(dataHelperService.findParticipantByIdOrThrowException(participantId))
                .thenReturn(Participant.builder()
                        .id(participantId)
                        .tournamentId(tournamentId)
                        .build());

        participantService.findById(tournamentId, participantId);
        verify(dataHelperService, times(1)).findParticipantByIdOrThrowException(participantId);

    }

    @Test
    public void findById_ExceptionFlow() {

        Long tournamentId = 1l;
        Long wrongTournamentId = 2l;
        Long participantId = 1l;

        when(dataHelperService.findParticipantByIdOrThrowException(participantId))
                .thenReturn(Participant.builder()
                        .id(participantId)
                        .tournamentId(tournamentId)
                        .build());

        Exception exception = assertThrows(ServiceException.class, () -> {
            participantService.findById(wrongTournamentId, participantId);
        });
        verify(dataHelperService, times(1)).findParticipantByIdOrThrowException(participantId);
        String expectedMessage = String.format("Participant (id '%s') doesn't belong to Tournament (id '%s')",
                participantId, wrongTournamentId);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void deleteTest1() {

        Long tournamentId = 1l;
        Long participantId = 1l;

        Optional<Match> optionalMatch = Optional.of(Match.builder()
                .build());
        when(matchService.findUncompletedMatchByParticipantId(tournamentId, participantId))
                .thenReturn(optionalMatch);

        participantService.delete(tournamentId, participantId);
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournamentId);
        verify(matchService, times(1)).findUncompletedMatchByParticipantId(tournamentId, participantId);
        verify(matchService, times(1)).disqualifyParticipantById(optionalMatch.get(), participantId);
        verify(participantRepository, times(1)).deleteByTournamentIdAndId(tournamentId, participantId);

    }

    @Test
    public void deleteTest2() {

        Long tournamentId = 1l;
        Long participantId = 1l;

        when(matchService.findUncompletedMatchByParticipantId(tournamentId, participantId))
                .thenReturn(Optional.empty());

        participantService.delete(tournamentId, participantId);
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournamentId);
        verify(matchService, times(1)).findUncompletedMatchByParticipantId(tournamentId, participantId);
        verify(matchService, Mockito.never()).disqualifyParticipantById(any(Match.class), anyLong());
        verify(participantRepository, times(1)).deleteByTournamentIdAndId(tournamentId, participantId);

    }

    @Test
    public void deleteAllByTournamentIdTest() {
        Long tournamentId = 1l;
        participantRepository.deleteAllByTournamentId(tournamentId);
        verify(participantRepository, times(1)).deleteAllByTournamentId(tournamentId);
    }

    @Test
    public void createAll_DuplicatesExceptionFlow() {

        Long tournamentId = 1l;
        ParticipantsAddForm participantsAddForm = ParticipantsAddForm.builder()
                .names(Arrays.asList("Player1", "Player1", "Player3", "Player4", "Player5", "Player6", "Player7"))
                .build();

        Exception exception = assertThrows(ServiceException.class, () -> {
            participantService.createAll(tournamentId, participantsAddForm);
        });

        verify(dataValidator, times(1)).validate(participantsAddForm);

        String expectedMessage = "Participants list has duplicates";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void createAll_LimitErrorFlow() {

        Long tournamentId = 1l;
        ParticipantsAddForm participantsAddForm = ParticipantsAddForm.builder()
                .names(Arrays.asList("Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7"))
                .build();

        when(dataValidator.validate(participantsAddForm))
                .thenReturn(ValidationResult.valid());

        when(dataHelperService.findTournamentByIdOrThrowException(1l)).thenReturn(
                Tournament.builder()
                        .id(tournamentId)
                        .maxNumberOfParticipants(8)
                        .build()
        );

        when(participantRepository.countByTournamentId(tournamentId))
                .thenReturn(2);

        Exception exception = assertThrows(ServiceException.class, () -> {
            participantService.createAll(tournamentId, participantsAddForm);
        });
        verify(dataValidator, times(1)).validate(participantsAddForm);
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournamentId);
        verify(participantRepository, times(1)).countByTournamentId(tournamentId);
        String expectedMessage = String.format(
                "Tournament (id '%s') can't get these participators. Limit will be exceeded", tournamentId);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void createAll_SuccessFlow() {

        Long tournamentId = 1l;
        ParticipantsAddForm participantsAddForm = ParticipantsAddForm.builder()
                .names(Arrays.asList("Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7"))
                .build();

        when(dataValidator.validate(participantsAddForm))
                .thenReturn(ValidationResult.valid());
        when(dataHelperService.findTournamentByIdOrThrowException(1l)).thenReturn(
                Tournament.builder()
                        .id(tournamentId)
                        .maxNumberOfParticipants(8)
                        .build()
        );

        when(participantRepository.countByTournamentId(tournamentId))
                .thenReturn(0);

        when(participantRepository.existsByNameAndTournamentId(any(), anyLong()))
                .thenReturn(false);

        participantService.createAll(tournamentId, participantsAddForm);
        verify(dataValidator, times(1)).validate(participantsAddForm);
        verify(participantRepository, times(1)).saveAll(any(List.class));
    }
}

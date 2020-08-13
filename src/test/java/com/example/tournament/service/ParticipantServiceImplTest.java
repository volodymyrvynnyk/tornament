package com.example.tournament.service;

import com.example.tournament.exception.ServiceException;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Participant;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.ParticipantRepository;
import com.example.tournament.util.mapper.ParticipantMapper;
import com.example.tournament.util.validation.DataValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParticipantServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private DataHelperService dataHelperService;

    @Mock
    private ParticipantMapper participantMapperMock;

    private ParticipantMapper participantMapper;

    private DataValidator dataValidator;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    @BeforeAll
    void setUp() {

        MockitoAnnotations.initMocks(this);

        participantMapper = new ParticipantMapper();

        dataValidator = new DataValidator();

        when(dataHelperService.findTournamentByIdOrThrowException(anyLong()))
                .thenReturn(Tournament.builder()
                        .id(1L)
                        .maxNumberOfParticipants(16)
                        .status(EventStatus.PENDING)
                        .title("Tournament")
                        .build());
//
//        when(participantRepository.findAllByTournamentId(anyLong()))
//                .thenReturn(new ArrayList<Participant>());
    }

    @Test
    public void findAllByTournamentIdTest() {

        Long tournamentId = 1l;
        participantService.findAllByTournamentId(tournamentId);
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
    public void findByIdSuccessFlow() {

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
    public void findByIdExceptionFlow() {

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
}

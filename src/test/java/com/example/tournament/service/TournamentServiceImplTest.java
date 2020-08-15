package com.example.tournament.service;

import com.example.tournament.dto.form.TournamentCreateForm;
import com.example.tournament.dto.response.TournamentDto;
import com.example.tournament.model.EventStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.Tournament;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.util.mapper.TournamentMapper;
import com.example.tournament.util.validation.DataValidator;
import com.example.tournament.util.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TournamentServiceImplTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ParticipantService participantService;

    @Mock
    private MatchService matchService;

    @Mock
    private DataHelperService dataHelperService;

    @Mock
    private TournamentMapper tournamentMapper;

    @Mock
    private DataValidator dataValidator;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findAllTest() {

        tournamentService.findAll();
        verify(tournamentRepository, times(1)).findAll();
    }

    @Test
    public void findByIdTest() {

        Long tournamentId = 1l;

        when(dataHelperService.findTournamentByIdOrThrowException(tournamentId))
                .thenReturn(Tournament.builder()
                        .id(tournamentId)
                        .build());

        when(tournamentMapper.tournamentToDto(any(Tournament.class)))
                .thenReturn(TournamentDto.builder()
                        .id(tournamentId)
                        .build());

        tournamentService.findById(tournamentId);
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournamentId);
    }

    @Test
    public void startTournamentTest() {

        Long tournamentId = 1l;
        when(dataHelperService.findTournamentByIdOrThrowException(tournamentId))
                .thenReturn(Tournament.builder()
                        .id(tournamentId)
                        .status(EventStatus.PENDING)
                        .build());

        when(participantService.countByTournamentId(tournamentId)).thenReturn(7);
        tournamentService.startTournament(tournamentId);
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournamentId);
        verify(participantService, times(1)).countByTournamentId(tournamentId);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
        verify(participantService, times(1)).findAllByTournamentId(tournamentId);
        verify(matchService, times(1)).generateMatches(anyList(), any(Tournament.class));
        verify(matchService, times(1)).findAllByTournamentId(tournamentId);
    }

    @Test
    public void createTest() {

        TournamentCreateForm tournamentCreateForm = TournamentCreateForm.builder()
                .title("Title")
                .maxNumberOfParticipants(16)
                .build();

        when(dataValidator.validate(tournamentCreateForm))
                .thenReturn(ValidationResult.valid());

        tournamentService.create(tournamentCreateForm);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    public void summarizeTournamentTest() {

        Tournament tournament = Tournament.builder()
                .id(1l)
                .status(EventStatus.STARTED)
                .build();

        when(dataHelperService.findTournamentByIdOrThrowException(tournament.getId()))
                .thenReturn(tournament);

        when(matchService.findFinalMatchByTournamentId(tournament.getId()))
                .thenReturn(Match.builder()
                        .tournamentId(tournament.getId())
                        .winnerId(1l)
                        .status(EventStatus.COMPLETED)
                        .winnerId(1l)
                        .build()
                );

        when(tournamentMapper.tournamentToDto(any(Tournament.class)))
                .thenReturn(TournamentDto.builder().build());

        tournamentService.summarizeTournament(tournament.getId());
        verify(dataHelperService, times(1)).findTournamentByIdOrThrowException(tournament.getId());
        verify(matchService, times(1)).findFinalMatchByTournamentId(tournament.getId());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    public void deleteTest() {

        Long tournamentId = 1l;

        when(dataHelperService.findTournamentByIdOrThrowException(tournamentId))
                .thenReturn(Tournament.builder()
                        .id(tournamentId)
                        .build());

        tournamentService.delete(tournamentId);
        verify(participantService, times(1)).deleteAllByTournamentId(tournamentId);
        verify(matchService, times(1)).deleteAllByTournamentId(tournamentId);
        verify(tournamentRepository, times(1)).deleteById(tournamentId);
    }
}

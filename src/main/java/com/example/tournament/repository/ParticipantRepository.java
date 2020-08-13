package com.example.tournament.repository;

import com.example.tournament.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findAllByTournamentId(Long tournamentId);

    int countByTournamentId(Long tournamentId);

    boolean existsByNameAndTournamentId(String name, Long tournamentId);

    void deleteAllByTournamentId(Long tournamentId);

    void deleteByTournamentIdAndId(Long tournamentId, Long id);
}

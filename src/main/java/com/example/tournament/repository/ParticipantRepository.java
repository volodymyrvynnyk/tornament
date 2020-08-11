package com.example.tournament.repository;

import com.example.tournament.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    boolean existsByName(String name);
}

package com.example.tournament.model;

import com.example.tournament.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Data
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Character label;

    private Long tournamentId;

    private Long firstParticipantId;

    private int firstParticipantScore;

    private Long secondParticipantId;

    private int secondParticipantScore;

    private Long winnerId;

    private Character nextMatchLabel;

    private LocalTime startTime;

    private LocalTime finishTime;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    public void addParticipant(Long participantId) {
        if (isNull(firstParticipantId)) {
            firstParticipantId = participantId;
        } else {
            if (isNull(secondParticipantId)) {
                secondParticipantId = participantId;
            } else {
                throw new ServiceException(String.format("Match (id '%s') already has got 2 participants", this.id));
            }
        }
    }

    public int getNumberOfParticipants() {
        int numberOfParticipants = 0;
        if (nonNull(firstParticipantId)) {
            numberOfParticipants++;
        }
        if (nonNull(secondParticipantId)) {
            numberOfParticipants++;
        }
        return numberOfParticipants;
    }

}

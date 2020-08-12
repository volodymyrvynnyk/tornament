package com.example.tournament.mapper;

import com.example.tournament.dto.response.MatchDto;
import com.example.tournament.model.Match;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatchMapper {

    public MatchDto matchToDto(Match match) {

        return MatchDto.builder()
                .id(match.getId())
                .label(match.getLabel())
                .nextMatchLabel(match.getNextMatchLabel())
                .firstParticipantId(match.getFirstParticipantId())
                .secondParticipantId(match.getSecondParticipantId())
                .score(String.format("%s:%s", match.getFirstParticipantScore(), match.getSecondParticipantScore()))
                .winnerId(match.getWinnerId())
                .start(match.getStartTime())
                .finish(match.getFinishTime())
                .build();
    }

    public List<MatchDto> matchListToDto(List<Match> matchList) {

        return matchList.stream().map(this::matchToDto).collect(Collectors.toList());
    }
}

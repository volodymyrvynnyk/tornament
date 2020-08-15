package com.example.tournament.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class MatchListDto {

    private List<MatchDto> matches;
}

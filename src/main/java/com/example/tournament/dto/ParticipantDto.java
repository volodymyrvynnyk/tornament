package com.example.tournament.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantDto {

    private Long id;

    private String name;
}

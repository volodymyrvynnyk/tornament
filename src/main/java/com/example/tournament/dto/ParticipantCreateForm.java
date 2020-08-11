package com.example.tournament.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantCreateForm {

    @NotBlank(message = "Name cannot be empty")
    private String name;
}

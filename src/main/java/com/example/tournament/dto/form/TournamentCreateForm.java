package com.example.tournament.dto.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentCreateForm {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private int maxNumberOfParticipants;

}

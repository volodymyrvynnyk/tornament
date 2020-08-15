package com.example.tournament.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentCreateForm {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @Min(value = 8, message = "Max number of participants must be >=8")
    private int maxNumberOfParticipants;

}

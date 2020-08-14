package com.example.tournament.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipantsAddForm {

    @Size(min = 1, message = "Min size of participants to add is 1")
    private List<String> names;
}

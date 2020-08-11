package com.example.tournament.service;

import com.example.tournament.dto.PageDto;
import com.example.tournament.dto.ParticipantCreateForm;
import com.example.tournament.dto.ParticipantDto;
import org.springframework.data.domain.Pageable;

public interface ParticipantService {

    PageDto<ParticipantDto> findAll(Pageable pageable);

    void create(ParticipantCreateForm participantCreateForm);

    void delete(Long id);

    ParticipantDto findById(Long id);

}

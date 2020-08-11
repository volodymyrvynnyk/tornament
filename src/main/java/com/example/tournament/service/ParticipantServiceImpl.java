package com.example.tournament.service;

import com.example.tournament.dto.PageDto;
import com.example.tournament.dto.ParticipantCreateForm;
import com.example.tournament.dto.ParticipantDto;
import com.example.tournament.exception.ServiceException;
import com.example.tournament.mapper.ParticipantMapper;
import com.example.tournament.model.Participant;
import com.example.tournament.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    private final ParticipantMapper participantMapper;

    private final static String BAD_ID_EXCEPTION = "There is no participant with this id";

    @Autowired
    public ParticipantServiceImpl(ParticipantRepository participantRepository, ParticipantMapper participantMapper) {
        this.participantRepository = participantRepository;
        this.participantMapper = participantMapper;
    }

    @Override
    public PageDto<ParticipantDto> findAll(Pageable pageable) {

        Page<Participant> participantPage = participantRepository.findAll(pageable);

        return new PageDto<ParticipantDto>().toBuilder()
                .pageNumber(participantPage.getNumber())
                .totalPages(participantPage.getTotalPages())
                .content(participantMapper.participantListToDto(participantPage.getContent()))
                .build();
    }

    @Override
    public void create(ParticipantCreateForm participantCreateForm) {

        if (participantRepository.existsByName(participantCreateForm.getName())) {
            throw new ServiceException(String.format("Participant '%s' already exists",
                    participantCreateForm.getName()));
        }

        Participant participant = Participant.builder()
                .name(participantCreateForm.getName())
                .build();

        participantRepository.save(participant);
    }


    @Override
    public void delete(Long id) {

        if (!participantRepository.existsById(id)) {
            throw new ServiceException(BAD_ID_EXCEPTION);
        }

        participantRepository.deleteById(id);
    }

    @Override
    public ParticipantDto findById(Long id) {

        Participant participant = participantRepository.findById(id).orElseThrow(() ->
                new ServiceException(BAD_ID_EXCEPTION));

        return participantMapper.participantToDto(participant);
    }

}

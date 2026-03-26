package it.unicam.cs.dto;

import it.unicam.cs.model.StatoHackathon;

import java.time.LocalDateTime;
import java.util.List;

public record HackathonCreazioneDTO(
    String nome,
    String regolamento,
    LocalDateTime scadenzaIscrizione,
    LocalDateTime dataInizio,
    LocalDateTime dataFine,
    String luogo,
    Double premioInDenaro,
    Integer dimensioneMassimoTeam,
    StatoHackathon stato,
    Long organizzatoreId,
    Long giudiceId,
    List<Long> mentoriIds
){}

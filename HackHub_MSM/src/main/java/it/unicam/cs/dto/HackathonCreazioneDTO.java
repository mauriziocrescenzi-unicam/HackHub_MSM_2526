package it.unicam.cs.dto;

import it.unicam.cs.model.StatoHackathon;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record HackathonCreazioneDTO(
        @NotNull String nome,
        @NotNull String regolamento,
        @NotNull LocalDateTime scadenzaIscrizione,
        @NotNull LocalDateTime dataInizio,
        @NotNull LocalDateTime dataFine,
        @NotNull String luogo,
        @NotNull Double premioInDenaro,
        @NotNull Integer dimensioneMassimoTeam,
        @NotNull StatoHackathon stato,
        @NotNull Long giudiceId,
        @NotNull List<Long> mentoriIds
){}

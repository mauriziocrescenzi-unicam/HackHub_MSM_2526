package it.unicam.cs.dto;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public record HackathonModificaDTO(
        @NotNull String nome,
        @NotNull String regolamento,
        @NotNull LocalDateTime scadenzaIscrizione,
        @NotNull LocalDateTime dataInizio,
        @NotNull LocalDateTime dataFine,
        @NotNull String luogo,
        @NotNull Double premioInDenaro
) {}

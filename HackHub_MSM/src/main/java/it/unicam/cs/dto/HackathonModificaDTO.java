package it.unicam.cs.dto;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
/**
 * DTO per la richiesta di modifica di un hackathon esistente.
 * Contiene solo i campi modificabili; i mentori
 * non sono inclusi in quanto gestiti da endpoint dedicati.
 *
 * @param nome               il nuovo nome dell'hackathon
 * @param regolamento        il nuovo testo del regolamento
 * @param scadenzaIscrizione la nuova data e ora di scadenza delle iscrizioni
 * @param dataInizio         la nuova data e ora di inizio
 * @param dataFine           la nuova data e ora di fine
 * @param luogo              il nuovo luogo dell'evento
 * @param premioInDenaro     il nuovo premio in denaro per i vincitori
 */

public record HackathonModificaDTO(
        @NotNull String nome,
        @NotNull String regolamento,
        @NotNull LocalDateTime scadenzaIscrizione,
        @NotNull LocalDateTime dataInizio,
        @NotNull LocalDateTime dataFine,
        @NotNull String luogo,
        @NotNull Double premioInDenaro
) {}

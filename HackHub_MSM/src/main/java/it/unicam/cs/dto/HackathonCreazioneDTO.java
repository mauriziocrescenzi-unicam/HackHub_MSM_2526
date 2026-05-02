package it.unicam.cs.dto;

import it.unicam.cs.model.StatoHackathon;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
/**
 * DTO per la richiesta di creazione di un nuovo hackathon.
 * Tutti i campi sono obbligatori. L'ID dell'organizzatore viene ricavato
 * automaticamente dall'account autenticato nel controller.
 *
 * @param nome                 il nome dell'hackathon
 * @param regolamento          il testo del regolamento
 * @param scadenzaIscrizione   la data e ora di scadenza delle iscrizioni
 * @param dataInizio           la data e ora di inizio dell'hackathon
 * @param dataFine             la data e ora di fine dell'hackathon
 * @param luogo                il luogo in cui si svolge l'hackathon
 * @param premioInDenaro       il premio in denaro per i vincitori
 * @param dimensioneMassimoTeam il numero massimo di membri per team
 * @param stato                lo stato iniziale dell'hackathon
 * @param giudiceId            l'ID dell'account da assegnare come giudice
 * @param mentoriIds           la lista degli ID degli account da assegnare come mentori
 */
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

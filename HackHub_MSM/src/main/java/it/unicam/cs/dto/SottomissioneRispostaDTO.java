package it.unicam.cs.dto;

import it.unicam.cs.model.Sottomissione;
import org.jetbrains.annotations.NotNull;
import java.time.LocalDateTime;

/**
 * DTO per la risposta contenente informazioni su una sottomissione.
 * Utilizzato per non esporre direttamente le entità JPA nelle API REST.
 * Nota: l'ID della sottomissione non è incluso per scelta progettuale.
 */
public record SottomissioneRispostaDTO(
        @NotNull String nome,
        @NotNull String link,
        @NotNull Long idTeam,
        @NotNull Long idHackathon,
        @NotNull LocalDateTime dataInvio,
        Integer voto,
        String giudizio,
        boolean valutata
) {
    /**
     * Factory method per creare un DTO da un'entità Sottomissione.
     *
     * @param s Entità Sottomissione da convertire
     * @return DTO con i dati della sottomissione (senza ID)
     */
    public static SottomissioneRispostaDTO fromSottomissione(Sottomissione s) {
        return new SottomissioneRispostaDTO(
                s.getNome(),
                s.getLink(),
                s.getIdTeam(),
                s.getIdHackathon(),
                s.getDataInvio(),
                s.getVoto(),
                s.getGiudizio(),
                s.getVoto() >= 0 && s.getGiudizio() != null
        );
    }
}
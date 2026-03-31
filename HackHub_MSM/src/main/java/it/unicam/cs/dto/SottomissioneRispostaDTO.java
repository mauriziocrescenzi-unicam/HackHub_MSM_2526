package it.unicam.cs.dto;

import it.unicam.cs.model.Sottomissione;

import java.time.LocalDateTime;

/**
 * DTO per la risposta contenente informazioni su una sottomissione.
 * Utilizzato per non esporre direttamente le entità JPA nelle API REST.
 */
public record SottomissioneRispostaDTO(
        String nome,
        String link,
        LocalDateTime dataInvio,
        Integer voto,
        String giudizio,
        boolean valutata
) {
    /**
     * Factory method per creare un DTO da un'entità Sottomissione.
     *
     * @param s Entità Sottomissione da convertire
     * @return DTO con i dati della sottomissione
     */
    public static SottomissioneRispostaDTO fromSottomissione(Sottomissione s) {
        return new SottomissioneRispostaDTO(
                s.getNome(),
                s.getLink(),
                s.getDataInvio(),
                s.getVoto(),
                s.getGiudizio(),
                s.getVoto() >= 0 && s.getGiudizio() != null
        );
    }
}
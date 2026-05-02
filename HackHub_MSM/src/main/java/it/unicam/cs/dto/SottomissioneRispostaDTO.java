package it.unicam.cs.dto;

import it.unicam.cs.model.Sottomissione;
import org.jetbrains.annotations.NotNull;
import java.time.LocalDateTime;

/**
 * DTO di risposta contenente le informazioni di una sottomissione.
 * Non espone direttamente l'entità JPA; l'ID della sottomissione non è incluso per scelta progettuale.
 * Il campo {@code valutata} indica se la sottomissione ha già ricevuto un voto e un giudizio.
 *
 * @param nome        il nome della sottomissione
 * @param link        il link al progetto
 * @param idTeam      l'ID del team che ha inviato la sottomissione
 * @param idHackathon l'ID dell'hackathon di riferimento
 * @param dataInvio   la data e ora dell'ultimo invio o aggiornamento
 * @param voto        il voto assegnato ({@code -1} se non ancora valutata)
 * @param giudizio    il giudizio scritto del giudice ({@code null} se non ancora valutata)
 * @param valutata    {@code true} se la sottomissione è già stata valutata, {@code false} altrimenti
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
     * Crea un DTO a partire da un'entità {@link Sottomissione}.
     *
     * @param s l'entità sottomissione da cui estrarre i dati
     * @return un nuovo {@link SottomissioneRispostaDTO} con tutti i campi popolati;
     *         {@code valutata} viene calcolato verificando che il voto sia maggiore o uguale a 0
     *         e che il giudizio non sia {@code null}
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
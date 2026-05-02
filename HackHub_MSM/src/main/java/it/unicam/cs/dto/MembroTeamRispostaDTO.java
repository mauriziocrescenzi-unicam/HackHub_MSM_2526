package it.unicam.cs.dto;

import it.unicam.cs.model.MembroTeam;
import java.time.LocalDateTime;

/**
 * DTO di risposta contenente le informazioni di un membro del team.
 * Non espone dati sensibili dell'utente come la password.
 *
 * @param idUtente      l'ID univoco dell'utente membro
 * @param nomeUtente    il nome dell'utente
 * @param cognomeUtente il cognome dell'utente
 * @param idTeam        l'ID del team a cui appartiene il membro
 * @param dataAdesione  la data e ora in cui l'utente è entrato nel team
 */
public record MembroTeamRispostaDTO(
        Long idUtente,
        String nomeUtente,
        String cognomeUtente,
        Long idTeam,
        LocalDateTime dataAdesione
) {
    /**
     * Crea un DTO a partire da un'entità {@link MembroTeam}.
     *
     * @param m l'entità membro del team da cui estrarre i dati
     * @return un nuovo {@link MembroTeamRispostaDTO} con tutti i campi popolati
     */
    public static MembroTeamRispostaDTO fromMembroTeam(MembroTeam m) {
        return new MembroTeamRispostaDTO(
                m.getAccount().getId(),
                m.getAccount().getNome(),
                m.getAccount().getCognome(),
                m.getTeam().getId(),
                m.getDataAdesione()
        );
    }
}
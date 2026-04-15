package it.unicam.cs.dto;

import it.unicam.cs.model.MembroTeam;
import java.time.LocalDateTime;

/**
 * DTO per la risposta contenente informazioni su un membro del team.
 * Non espone dati sensibili dell'utente (es. password).
 */
public record MembroTeamRispostaDTO(
        Long idUtente,
        String nomeUtente,
        String cognomeUtente,
        Long idTeam,
        LocalDateTime dataAdesione
) {
    /**
     * Factory method per creare un DTO da un'entità MembroTeam.
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
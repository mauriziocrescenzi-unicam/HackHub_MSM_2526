package it.unicam.cs.dto;

/**
 * DTO per la richiesta di abbandono di un team da parte di un membro.
 * Contiene gli ID necessari per identificare il membro e il team.
 */
public record AbbandonaTeamDTO(
        Long idMembro,
        Long idTeam
) {
    /**
     * Validazione base dei parametri.
     */
    public AbbandonaTeamDTO {
        if (idMembro == null || idMembro <= 0) {
            throw new IllegalArgumentException("idMembro deve essere positivo");
        }
        if (idTeam == null || idTeam <= 0) {
            throw new IllegalArgumentException("idTeam deve essere positivo");
        }
    }
}
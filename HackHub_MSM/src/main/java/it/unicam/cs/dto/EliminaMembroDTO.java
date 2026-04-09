package it.unicam.cs.dto;

/**
 * DTO per la richiesta di eliminazione di un membro da un team.
 * Contiene gli ID necessari per identificare chi elimina e chi viene eliminato.
 *
 * @param idMembroCheElimina ID del membro che sta effettuando l'eliminazione
 * @param idMembroDaEliminare ID del membro da eliminare dal team
 * @param idTeam ID del team da cui rimuovere il membro
 */
public record EliminaMembroDTO(
        Long idMembroCheElimina,
        Long idMembroDaEliminare,
        Long idTeam
) {
    /**
     * Validazione automatica dei parametri nel costruttore compatto.
     * Viene eseguita ogni volta che viene istanziato il DTO.
     */
    public EliminaMembroDTO {
        if (idMembroCheElimina == null || idMembroCheElimina <= 0) {
            throw new IllegalArgumentException("idMembroCheElimina deve essere positivo e non null");
        }
        if (idMembroDaEliminare == null || idMembroDaEliminare <= 0) {
            throw new IllegalArgumentException("idMembroDaEliminare deve essere positivo e non null");
        }
        if (idTeam == null || idTeam <= 0) {
            throw new IllegalArgumentException("idTeam deve essere positivo e non null");
        }
        if (idMembroCheElimina.equals(idMembroDaEliminare)) {
            throw new IllegalArgumentException("Un membro non può eliminare se stesso dal team");
        }
    }
}
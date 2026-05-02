package it.unicam.cs.dto;

/**
 * DTO per la richiesta di supporto inviata da un team a un mentore durante un hackathon.
 * L'ID del team richiedente e la data di invio vengono gestiti automaticamente dal controller.
 *
 * @param descrizioneRichiesta la descrizione del supporto richiesto; non può essere {@code null} o vuota
 * @param idHackathon          l'ID dell'hackathon di riferimento; deve essere un valore positivo
 */
public record RichiestaSupportoInvioDTO(
        String descrizioneRichiesta,
        Long idHackathon
) {
    /**
     * Costruttore compatto con validazione automatica dei parametri.
     *
     * @throws IllegalArgumentException se {@code descrizioneRichiesta} è {@code null} o vuota,
     *                                  oppure se {@code idHackathon} è {@code null} o non positivo
     */
    public RichiestaSupportoInvioDTO {
        if (descrizioneRichiesta == null || descrizioneRichiesta.trim().isEmpty()) {
            throw new IllegalArgumentException("descrizioneRichiesta non può essere vuota");
        }
        if (idHackathon == null || idHackathon <= 0) {
            throw new IllegalArgumentException("idHackathon deve essere positivo");
        }
    }
}
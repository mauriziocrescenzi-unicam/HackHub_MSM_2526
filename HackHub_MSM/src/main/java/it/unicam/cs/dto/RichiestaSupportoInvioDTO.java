package it.unicam.cs.dto;

/**
 * DTO per la richiesta di supporto da parte di un team a un mentore.
 * idMembroTeam e dataInvio sono gestiti automaticamente dal controller.
 */
public record RichiestaSupportoInvioDTO(
        String descrizioneRichiesta,
        Long idHackathon
) {
    /**
     * Validazione automatica dei parametri.
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
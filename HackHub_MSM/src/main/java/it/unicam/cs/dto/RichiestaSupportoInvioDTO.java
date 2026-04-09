package it.unicam.cs.dto;

import it.unicam.cs.model.RichiestaSupporto;

import java.time.LocalDateTime;

/**
 * DTO per la richiesta di supporto da parte di un team a un mentore.
 */
public record RichiestaSupportoInvioDTO(
        Long idMembroTeam,
        String descrizioneRichiesta,
        LocalDateTime dataInvio,
        Long idHackathon
) {
    /**
     * Validazione automatica dei parametri.
     */
    public RichiestaSupportoInvioDTO {
        if (idMembroTeam == null || idMembroTeam <= 0) {
            throw new IllegalArgumentException("idMembroTeam deve essere positivo");
        }
        if (descrizioneRichiesta == null || descrizioneRichiesta.trim().isEmpty()) {
            throw new IllegalArgumentException("descrizioneRichiesta non può essere vuota");
        }
        if (dataInvio == null) {
            throw new IllegalArgumentException("dataInvio non può essere null");
        }
        if (idHackathon == null || idHackathon <= 0) {
            throw new IllegalArgumentException("idHackathon deve essere positivo");
        }
    }
}
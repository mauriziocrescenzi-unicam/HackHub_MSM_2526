package it.unicam.cs.dto;

import it.unicam.cs.model.Invito;
import it.unicam.cs.model.StatoInvito;
import java.time.LocalDateTime;

public record InvitoRispostaDTO(
        long id,
        int idUtenteMittente,
        int idUtenteDestinatario,
        LocalDateTime dataInvio,
        StatoInvito stato
) {
    public static InvitoRispostaDTO fromInvito(Invito i) {
        return new InvitoRispostaDTO(
                i.getId(),
                i.getIdUtenteMittente(),
                i.getIdUtenteDestinatario(),
                i.getDataInvio(),
                i.getStato()
        );
    }
}
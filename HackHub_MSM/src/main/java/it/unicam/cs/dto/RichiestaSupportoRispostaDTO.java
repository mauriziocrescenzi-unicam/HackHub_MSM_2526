package it.unicam.cs.dto;

import it.unicam.cs.model.RichiestaSupporto;
import java.time.LocalDateTime;

public record RichiestaSupportoRispostaDTO(
        Long id,
        String descrizioneRichiesta,
        LocalDateTime dataInvio,
        String descrizioneRisposta,
        Long idTeam,
        Long idHackathon
) {
    public static RichiestaSupportoRispostaDTO fromRichiestaSupporto(RichiestaSupporto r) {
        return new RichiestaSupportoRispostaDTO(
                r.getId(),
                r.getDescrizioneRichiesta(),
                r.getDataInvio(),
                r.getDescrizioneRisposta(),
                r.getIdTeam(),
                r.getHackathon().getId()
        );
    }
}
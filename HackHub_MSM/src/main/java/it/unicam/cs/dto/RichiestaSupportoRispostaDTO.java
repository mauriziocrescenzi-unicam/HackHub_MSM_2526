package it.unicam.cs.dto;

import it.unicam.cs.model.RichiestaSupporto;
import java.time.LocalDateTime;
/**
 * DTO di risposta contenente le informazioni di una richiesta di supporto.
 * Il campo {@code descrizioneRisposta} è {@code null} se la richiesta non è ancora stata risolta.
 *
 * @param id                   l'ID univoco della richiesta
 * @param descrizioneRichiesta la descrizione del supporto richiesto dal team
 * @param dataInvio            la data e ora di invio della richiesta
 * @param descrizioneRisposta  la risposta del mentore, oppure {@code null} se non ancora fornita
 * @param idTeam               l'ID del team richiedente
 * @param idHackathon          l'ID dell'hackathon di riferimento
 */
public record RichiestaSupportoRispostaDTO(
        Long id,
        String descrizioneRichiesta,
        LocalDateTime dataInvio,
        String descrizioneRisposta,
        Long idTeam,
        Long idHackathon
) {
    /**
     * Crea un DTO a partire da un'entità {@link RichiestaSupporto}.
     *
     * @param r l'entità richiesta di supporto da cui estrarre i dati
     * @return un nuovo {@link RichiestaSupportoRispostaDTO} con tutti i campi popolati
     */
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
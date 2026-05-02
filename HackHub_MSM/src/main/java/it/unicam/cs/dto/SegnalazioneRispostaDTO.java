package it.unicam.cs.dto;

import it.unicam.cs.model.Segnalazione;
import it.unicam.cs.model.StatoSegnalazione;
/**
 * DTO di risposta contenente le informazioni di una segnalazione effettuata da un mentore.
 *
 * @param stato            lo stato corrente della segnalazione
 * @param dataSegnalazione la data e ora della segnalazione in formato stringa
 * @param motivazione      la motivazione della segnalazione
 * @param idTeam           l'ID del team segnalato
 * @param idMentore        l'ID del mentore che ha effettuato la segnalazione
 * @param hackathon        i dati dell'hackathon di riferimento
 */
public record SegnalazioneRispostaDTO (
    StatoSegnalazione stato,
    String dataSegnalazione,
    String motivazione,
    long idTeam,
    long idMentore,
    HackathonRispostaDTO hackathon
){
    /**
     * Crea un DTO a partire da un'entità {@link Segnalazione}.
     *
     * @param segnalazione l'entità segnalazione da cui estrarre i dati
     * @return un nuovo {@link SegnalazioneRispostaDTO} con tutti i campi popolati
     */
    public static SegnalazioneRispostaDTO fromSegnalazione(Segnalazione segnalazione){
        return new SegnalazioneRispostaDTO(segnalazione.getStato(),segnalazione.getDataSegnalazione().toString(),segnalazione.getMotivazione(),segnalazione.getTeam().getId(),segnalazione.getMentore().getId(),HackathonRispostaDTO.fromHackathon(segnalazione.getHackathon()));
    }
}

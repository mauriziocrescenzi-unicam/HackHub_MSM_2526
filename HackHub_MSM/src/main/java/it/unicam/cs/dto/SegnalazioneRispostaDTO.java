package it.unicam.cs.dto;

import it.unicam.cs.model.StatoSegnalazione;

public record SegnalazioneRispostaDTO (
    StatoSegnalazione stato,
    String dataSegnalazione,
    String motivazione,
    long idTeam,
    long idMentore,
    HackathonRispostaDTO hackathon
){
    public static SegnalazioneRispostaDTO fromSegnalazione(it.unicam.cs.model.Segnalazione segnalazione){
        return new SegnalazioneRispostaDTO(segnalazione.getStato(),segnalazione.getDataSegnalazione().toString(),segnalazione.getMotivazione(),segnalazione.getTeam().getId(),segnalazione.getMentore().getId(),HackathonRispostaDTO.fromHackathon(segnalazione.getHackathon()));
    }
}

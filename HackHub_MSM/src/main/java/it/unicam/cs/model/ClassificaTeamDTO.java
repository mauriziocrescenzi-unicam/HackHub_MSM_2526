package it.unicam.cs.model;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO per rappresentare un team nella classifica di un hackathon.
 */
@Getter
@Setter
public class ClassificaTeamDTO {

    private final Team team;

    private final double punteggio;

    private final String giudizio;

    private int posizione;

    /**
     * Costruisce un DTO per la classifica.
     *
     * @param team Team classificato
     * @param punteggio Punteggio ottenuto
     * @param giudizio Giudizio scritto
     */
    public ClassificaTeamDTO(Team team, double punteggio, String giudizio) {
        this.team = team;
        this.punteggio = punteggio;
        this.giudizio = giudizio;
        this.posizione = 0; // Verrà assegnato dopo l'ordinamento
    }

    @Override
    public String toString() {
        return "ClassificaTeamDTO{" +
                "posizione=" + posizione +
                ", team=" + (team != null ? team.getNome() : "null") +
                ", punteggio=" + punteggio +
                ", giudizio='" + giudizio + '\'' +
                '}';
    }
}
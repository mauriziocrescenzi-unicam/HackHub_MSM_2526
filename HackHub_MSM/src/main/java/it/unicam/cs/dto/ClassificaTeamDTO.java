package it.unicam.cs.dto;

import it.unicam.cs.model.Team;

/**
 * DTO per rappresentare un team nella classifica di un hackathon.
 */
public record ClassificaTeamDTO(
        String team,
        double punteggio,
        String giudizio,
        int posizione
) {
    /**
     * Costruttore compatto: posizione inizializzata a 0 (verrà assegnata dopo l'ordinamento).
     */
    public ClassificaTeamDTO(Team team, double punteggio, String giudizio) {
        this(team.getNome(), punteggio, giudizio, 0);
    }

    /**
     * Restituisce una copia del record con la posizione aggiornata.
     */
    public ClassificaTeamDTO withPosizione(int nuovaPosizione) {
        return new ClassificaTeamDTO(team, punteggio, giudizio, nuovaPosizione);
    }

    @Override
    public String toString() {
        return "ClassificaTeamDTO{" +
                "posizione=" + posizione +
                ", team=" + (team != null ? team : "null") +
                ", punteggio=" + punteggio +
                ", giudizio='" + giudizio + '\'' +
                '}';
    }
}
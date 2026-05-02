package it.unicam.cs.dto;

import it.unicam.cs.model.Team;

/**
 * DTO per rappresentare la posizione di un team nella classifica di un hackathon.
 *
 * @param team      il nome del team
 * @param punteggio il punteggio ottenuto dalla valutazione
 * @param giudizio  il giudizio scritto del giudice
 * @param posizione la posizione in classifica
 */
public record ClassificaTeamDTO(
        String team,
        double punteggio,
        String giudizio,
        int posizione
) {
    /**
     * Costruttore compatto che crea un DTO a partire da un'entità {@link Team}.
     * La posizione viene inizializzata a {@code 0} e sarà aggiornata dopo l'ordinamento
     * tramite {@link #withPosizione(int)}.
     *
     * @param team      l'entità team di cui recuperare il nome
     * @param punteggio il punteggio ottenuto
     * @param giudizio  il giudizio scritto del giudice
     */
    public ClassificaTeamDTO(Team team, double punteggio, String giudizio) {
        this(team.getNome(), punteggio, giudizio, 0);
    }

    /**
     * Restituisce una copia di questo record con la posizione aggiornata al valore specificato.
     *
     * @param nuovaPosizione la posizione da assegnare
     * @return un nuovo {@link ClassificaTeamDTO} con la posizione aggiornata
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
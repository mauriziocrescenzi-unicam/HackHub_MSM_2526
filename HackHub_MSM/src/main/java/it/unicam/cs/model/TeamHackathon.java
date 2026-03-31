package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Entità che rappresenta l'associazione tra un Team e un Hackathon.
 * Contiene lo stato di iscrizione del team all'hackathon.
 * Classe modello passiva: la logica di gestione è delegata al TeamController.
 *
 */
@Entity
@Getter
@Setter
public class TeamHackathon implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_hackathon_seq")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;
    private boolean iscritto;

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public TeamHackathon() {}

    /**
     * Costruttore per creare una nuova associazione Team-Hackathon.
     * Imposta automaticamente iscritto a true.
     *
     * @param team Team da associare
     * @param hackathon Hackathon a cui associare il team
     */
    public TeamHackathon(Team team, Hackathon hackathon) {
        this.team = team;
        this.hackathon = hackathon;
        this.iscritto = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamHackathon that = (TeamHackathon) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TeamHackathon{" +
                "id=" + id +
                ", iscritto=" + iscritto +
                '}';
    }
}
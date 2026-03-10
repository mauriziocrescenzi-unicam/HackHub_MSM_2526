package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;

/**
 * Entità che rappresenta l'associazione tra un Team e un Hackathon.
 * Contiene lo stato di iscrizione del team all'hackathon.
 * Classe modello passiva: la logica di gestione è delegata al TeamController.
 *
 */
@Entity
@Getter
@Table(name = "team_hackathon")
public class TeamHackathon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_hackathon_seq")
    @SequenceGenerator(name = "team_hackathon_seq", sequenceName = "team_hackathon_sequence", allocationSize = 1)
    private Long id;

    /**
     * Riferimento al Team associato.
     */
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Riferimento all'Hackathon associato.
     */
    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    /**
     * Stato di iscrizione: true se il team è iscritto, false altrimenti.
     */
    @Column(name = "iscritto", nullable = false)
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

    // ==================== SETTER ====================

    public void setId(Long id) {
        this.id = id;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setHackathon(Hackathon hackathon) {
        this.hackathon = hackathon;
    }

    public void setIscritto(boolean iscritto) {
        this.iscritto = iscritto;
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
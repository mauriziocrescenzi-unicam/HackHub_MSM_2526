package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità che rappresenta la relazione tra un Utente e un Team.
 * Ogni istanza indica che un utente è membro di un team con un ruolo specifico.
 * Utilizza una chiave primaria composta (MembroTeamId).
 * Classe modello passiva: la logica di gestione è delegata al MembroTeamController.
 *
 */
@Entity
@Getter
@Table(name = "membro_team")
public class MembroTeam implements Serializable {

    @EmbeddedId
    private MembroTeamId id;

    @ManyToOne
    @MapsId("utenteId") // Collega il campo utenteId della chiave composta
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne
    @MapsId("teamId") // Collega il campo teamId della chiave composta
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "data_adesione")
    private LocalDateTime dataAdesione;

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public MembroTeam() {}

    /**
     * Costruttore per creare una nuova associazione utente-team.
     * Inizializza automaticamente la chiave composta con gli ID delle entità.
     *
     * @param utente Utente da associare
     * @param team Team a cui associare l'utente
     */
    public MembroTeam(Utente utente, Team team) {
        this.utente = utente;
        this.team = team;
        this.dataAdesione = LocalDateTime.now();
        this.id = new MembroTeamId(utente.getId(), team.getId());
    }

    // ==================== SETTER ====================

    /**
     * Aggiorna il riferimento all'utente e sincronizza la chiave composta.
     * Mantiene la consistenza tra l'entità e l'ID embedded.
     */
    public void setUtente(Utente utente) {
        this.utente = utente;
        if (utente != null && id != null) {
            id.setUtenteId(utente.getId());
        }
    }

    /**
     * Aggiorna il riferimento al team e sincronizza la chiave composta.
     * Mantiene la consistenza tra l'entità e l'ID embedded.
     */
    public void setTeam(Team team) {
        this.team = team;
        if (team != null && id != null) {
            id.setTeamId(team.getId());
        }
    }

    public void setDataAdesione(LocalDateTime dataAdesione) {
        this.dataAdesione = dataAdesione;
    }

    // ==================== METODI DI UTILITÀ ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembroTeam that = (MembroTeam) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MembroTeam{" +
                ", dataAdesione=" + dataAdesione +
                '}';
    }
}
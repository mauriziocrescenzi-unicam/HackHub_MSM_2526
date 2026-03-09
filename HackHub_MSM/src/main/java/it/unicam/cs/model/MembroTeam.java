package it.unicam.cs.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "membro_team")
public class MembroTeam implements Serializable {

    @EmbeddedId
    private MembroTeamId id;

    @ManyToOne
    @MapsId("utenteId") // Collega questo campo al campo utenteId nella chiave composta
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne
    @MapsId("teamId") // Collega questo campo al campo teamId nella chiave composta
    @JoinColumn(name = "team_id")
    private Team team;

    private String ruolo;
    
    @Column(name = "data_adesione")
    private LocalDateTime dataAdesione;

    public MembroTeam() {}

    public MembroTeam(Utente utente, Team team, String ruolo) {
        this.utente = utente;
        this.team = team;
        this.ruolo = ruolo;
        this.dataAdesione = LocalDateTime.now();
        // Inizializza la chiave composta con gli ID delle entità
        this.id = new MembroTeamId(utente.getId(), team.getId());
    }

    // Getters e Setters
    public MembroTeamId getId() { return id; }
    public void setId(MembroTeamId id) { this.id = id; }
    
    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { 
        this.utente = utente;
        if (utente != null && id != null) id.setUtenteId(utente.getId());
    }
    
    public Team getTeam() { return team; }
    public void setTeam(Team team) { 
        this.team = team;
        if (team != null && id != null) id.setTeamId(team.getId());
    }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public LocalDateTime getDataAdesione() { return dataAdesione; }
    public void setDataAdesione(LocalDateTime dataAdesione) { this.dataAdesione = dataAdesione; }

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
}
package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class MembroTeam {
    private int id;
    private int idTeam;
    private int idUtente;
    private String ruolo;
    private LocalDateTime dataAdesione;
    private Team team;
    private Utente utente;

    public MembroTeam(int id, int idTeam, int idUtente, String ruolo) {
        this.id = id;
        this.idTeam = idTeam;
        this.idUtente = idUtente;
        this.ruolo = ruolo;
        this.dataAdesione = LocalDateTime.now();
    }

    public MembroTeam(int id, Team team, Utente utente, String ruolo) {
        this.id = id;
        this.team = team;
        this.utente = utente;
        if (team != null) {
            this.idTeam = team.getId();
        }
        if (utente != null) {
            this.idUtente = utente.getId();
        }
        this.ruolo = ruolo;
        this.dataAdesione = LocalDateTime.now();
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public int getIdTeam() {
        return idTeam;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public LocalDateTime getDataAdesione() {
        return dataAdesione;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
        if (team != null) {
            this.idTeam = team.getId();
        }
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
        if (utente != null) {
            this.idUtente = utente.getId();
        }
    }

    // Metodo dal diagramma
    public void iscrivereTeam() {
        // Logica per iscrizione team (può essere implementata in TeamController)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembroTeam that = (MembroTeam) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MembroTeam{" +
                "id=" + id +
                ", idTeam=" + idTeam +
                ", idUtente=" + idUtente +
                ", ruolo='" + ruolo + '\'' +
                ", dataAdesione=" + dataAdesione +
                '}';
    }
}
package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {
    private int id;
    private String nome;
    private String descrizione;
    private LocalDateTime dataCreazione;
    private int amministratore;
    private List<MembroTeam> membri;
    private List<TeamHackathon> hackathonIscritti;

    public Team(int id, String nome, String descrizione, int amministratore) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.dataCreazione = LocalDateTime.now();
        this.amministratore = amministratore;
        this.membri = new ArrayList<>();
        this.hackathonIscritti = new ArrayList<>();
    }

    // Metodi dal diagramma di progetto
    public boolean iscritto(Hackathon hackathon) {
        if (hackathon == null) return false;
        for (TeamHackathon th : hackathonIscritti) {
            if (th.getHackathon() != null && th.getHackathon().equals(hackathon)) {
                return true;
            }
        }
        return false;
    }

    public int getMembri() {
        return membri.size();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public int getAmministratore() {
        return amministratore;
    }

    public void setAmministratore(int amministratore) {
        this.amministratore = amministratore;
    }

    // Metodi aggiuntivi per gestione interna
    public int getId() {
        return id;
    }

    public List<MembroTeam> getMembriList() {
        return membri;
    }

    public List<TeamHackathon> getHackathonIscritti() {
        return hackathonIscritti;
    }

    /* public void aggiungiMembro(MembroTeam membro) {
        if (membro != null && !this.membri.contains(membro)) {
            this.membri.add(membro);
        }
    }

    public void aggiungiHackathon(TeamHackathon teamHackathon) {
        if (teamHackathon != null && !this.hackathonIscritti.contains(teamHackathon)) {
            this.hackathonIscritti.add(teamHackathon);
        }
    } */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", dataCreazione=" + dataCreazione +
                ", amministratore=" + amministratore +
                ", numeroMembri=" + membri.size() +
                '}';
    }
}
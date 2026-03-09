package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Getter
@Entity

public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String nome;
    private String descrizione;
    private LocalDateTime dataCreazione;
    private int amministratore;
    // da aggiungere sul Class Diagram di progetto iterazione1
    private List<MembroTeam> membri;
    private List<TeamHackathon> hackathonIscritti;

    public Team(String nome, String descrizione, int amministratore) {
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

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public void setAmministratore(int amministratore) {
        this.amministratore = amministratore;
    }

    public List<MembroTeam> getMembriList() {
        return membri;
    }

    public List<TeamHackathon> getHackathonIscritti() {
        return hackathonIscritti;
    }

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
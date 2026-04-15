package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
@Entity
@Getter
public class Segnalazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    StatoSegnalazione stato;
    LocalDateTime dataSegnalazione;
    String motivazione;
    @ManyToOne
    Team team;
    @ManyToOne
    Account mentore;
    @ManyToOne
    Hackathon hackathon;

    public Segnalazione() {
    }

    public Segnalazione(StatoSegnalazione stato, LocalDateTime dataSegnalazione, String motivazione, Team team, Account mentore, Hackathon hackathon) {
        this.stato = stato;
        this.dataSegnalazione = dataSegnalazione;
        this.motivazione = motivazione;
        this.team = team;
        this.mentore = mentore;
        this.hackathon = hackathon;
    }

    public void setStato(StatoSegnalazione stato) {
        if(stato==null) throw new IllegalArgumentException("Stato non valido");
        this.stato = stato;
    }

    public void setDataSegnalazione(LocalDateTime dataSegnalazione) {
        if(dataSegnalazione==null) throw new IllegalArgumentException("Data non valida");
        if(dataSegnalazione.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Data passata");
        this.dataSegnalazione = dataSegnalazione;
    }

    public void setMotivazione(String motivazione) {
        if(motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
        this.motivazione = motivazione;
    }

    public void setTeam(Team team) {
        if(team==null) throw new IllegalArgumentException("Team non valido");
        this.team = team;
    }

    public void setMentore(Account mentore) {
        if(mentore==null) throw new IllegalArgumentException("Mentore non valido");
        this.mentore = mentore;
    }

    public void setHackathon(Hackathon hackathon) {
        if(hackathon==null) throw new IllegalArgumentException("Hackathon non valido");
        this.hackathon = hackathon;
    }


}

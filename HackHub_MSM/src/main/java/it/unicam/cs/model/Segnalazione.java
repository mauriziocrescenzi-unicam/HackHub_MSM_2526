package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
/**
 * Entità che rappresenta una segnalazione effettuata da un mentore nei confronti
 * di un team durante un hackathon, per violazione del regolamento.
 * Lo stato della segnalazione segue il ciclo:
 */
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
    /** Costruttore vuoto richiesto da JPA. */
    public Segnalazione() {
    }
    /**
     * Costruttore per creare una nuova segnalazione con tutti i dati necessari.
     *
     * @param stato             lo stato iniziale della segnalazione
     * @param dataSegnalazione  la data e ora in cui è avvenuta la segnalazione
     * @param motivazione       la motivazione della segnalazione
     * @param team              il team segnalato
     * @param mentore           il mentore che ha effettuato la segnalazione
     * @param hackathon         l'hackathon di riferimento
     */
    public Segnalazione(StatoSegnalazione stato, LocalDateTime dataSegnalazione, String motivazione, Team team, Account mentore, Hackathon hackathon) {
        this.stato = stato;
        this.dataSegnalazione = dataSegnalazione;
        this.motivazione = motivazione;
        this.team = team;
        this.mentore = mentore;
        this.hackathon = hackathon;
    }
    /**
     * Imposta lo stato della segnalazione.
     *
     * @param stato il nuovo stato; non può essere {@code null}
     * @throws IllegalArgumentException se lo stato è {@code null}
     */
    public void setStato(StatoSegnalazione stato) {
        if(stato==null) throw new IllegalArgumentException("Stato non valido");
        this.stato = stato;
    }
    /**
     * Imposta la data e ora della segnalazione.
     * La data non può essere nel passato rispetto al momento corrente.
     *
     * @param dataSegnalazione la data della segnalazione; non può essere {@code null} o nel passato
     * @throws IllegalArgumentException se la data è {@code null} o precedente al momento corrente
     */
    public void setDataSegnalazione(LocalDateTime dataSegnalazione) {
        if(dataSegnalazione==null) throw new IllegalArgumentException("Data non valida");
        if(dataSegnalazione.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Data passata");
        this.dataSegnalazione = dataSegnalazione;
    }
    /**
     * Imposta la motivazione della segnalazione.
     *
     * @param motivazione la motivazione; non può essere vuota
     * @throws IllegalArgumentException se la motivazione è vuota
     */
    public void setMotivazione(String motivazione) {
        if(motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
        this.motivazione = motivazione;
    }
    /**
     * Imposta il team oggetto della segnalazione.
     *
     * @param team il team segnalato; non può essere {@code null}
     * @throws IllegalArgumentException se il team è {@code null}
     */
    public void setTeam(Team team) {
        if(team==null) throw new IllegalArgumentException("Team non valido");
        this.team = team;
    }
    /**
     * Imposta il mentore che ha effettuato la segnalazione.
     *
     * @param mentore l'account del mentore; non può essere {@code null}
     * @throws IllegalArgumentException se il mentore è {@code null}
     */
    public void setMentore(Account mentore) {
        if(mentore==null) throw new IllegalArgumentException("Mentore non valido");
        this.mentore = mentore;
    }
    /**
     * Imposta l'hackathon di riferimento della segnalazione.
     *
     * @param hackathon l'hackathon associato; non può essere {@code null}
     * @throws IllegalArgumentException se l'hackathon è {@code null}
     */
    public void setHackathon(Hackathon hackathon) {
        if(hackathon==null) throw new IllegalArgumentException("Hackathon non valido");
        this.hackathon = hackathon;
    }


}

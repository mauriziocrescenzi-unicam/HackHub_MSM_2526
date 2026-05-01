package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
/**
 * Entità che rappresenta un account utente nel sistema HackHub.
 * Ogni account è identificato da un'email univoca e possiede un ruolo
 * che determina le operazioni consentite nel sistema.
 */
@Getter
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String nome;
    private String cognome;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ruolo ruolo;
    /** Costruttore vuoto richiesto da JPA. */
    public Account() {}
    /**
     * Costruttore per creare un nuovo account con tutti i dati obbligatori.
     *
     * @param email    l'email univoca dell'account
     * @param password la password (già cifrata)
     * @param ruolo    il ruolo assegnato all'account
     * @param nome     il nome dell'utente
     * @param cognome  il cognome dell'utente
     */
    public Account(String email, String password, Ruolo ruolo, String nome, String cognome) {
        this.email    = email;
        this.password = password;
        this.ruolo    = ruolo;
        this.nome     = nome;
        this.cognome  = cognome;
    }
    /**
     * Aggiorna l'email dell'account.
     *
     * @param email la nuova email; non può essere {@code null} o vuota
     * @throws IllegalArgumentException se l'email è {@code null} o vuota
     */
    public void setEmail(String email) {
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("Email non valida");
        this.email = email;
    }
    /**
     * Aggiorna il nome dell'utente.
     *
     * @param nome il nuovo nome; non può essere {@code null} o vuoto
     * @throws IllegalArgumentException se il nome è {@code null} o vuoto
     */
    public void setNome(String nome) {
        if (nome == null || nome.isEmpty()) throw new IllegalArgumentException("Nome non valido");
        this.nome = nome;
    }
    /**
     * Aggiorna il cognome dell'utente.
     *
     * @param cognome il nuovo cognome; non può essere {@code null} o vuoto
     * @throws IllegalArgumentException se il cognome è {@code null} o vuoto
     */
    public void setCognome(String cognome) {
        if (cognome == null || cognome.isEmpty()) throw new IllegalArgumentException("Cognome non valido");
        this.cognome = cognome;
    }
    /**
     * Aggiorna la password dell'account.
     *
     * @param password la nuova password (già cifrata); non può essere {@code null} o vuota
     * @throws IllegalArgumentException se la password è {@code null} o vuota
     */
    public void setPassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password non valida");
        this.password = password;
    }
    /**
     * Cambia il ruolo dell'account.
     *
     * @param nuovoRuolo il nuovo ruolo da assegnare
     */
    public void cambiaRuolo(Ruolo nuovoRuolo) {
        this.ruolo = nuovoRuolo;
    }
}
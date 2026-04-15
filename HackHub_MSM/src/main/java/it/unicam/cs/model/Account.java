package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

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

    public Account() {}

    public Account(String email, String password, Ruolo ruolo, String nome, String cognome) {
        this.email    = email;
        this.password = password;
        this.ruolo    = ruolo;
        this.nome     = nome;
        this.cognome  = cognome;
    }

    public void setEmail(String email) {
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("Email non valida");
        this.email = email;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isEmpty()) throw new IllegalArgumentException("Nome non valido");
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        if (cognome == null || cognome.isEmpty()) throw new IllegalArgumentException("Cognome non valido");
        this.cognome = cognome;
    }

    public void setPassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password non valida");
        this.password = password;
    }

    public void cambiaRuolo(Ruolo nuovoRuolo) {
        this.ruolo = nuovoRuolo;
    }
}
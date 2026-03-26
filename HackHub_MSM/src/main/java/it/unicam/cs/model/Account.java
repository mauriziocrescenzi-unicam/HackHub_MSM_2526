package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String nome;
    private String cognome;

    public Account(String email, String nome, String cognome) {
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
    }
    public Account() {
    }

    public void setEmail(String email) {
        if(email.isEmpty()) throw new IllegalArgumentException();
        this.email = email;
    }

    public void setNome(String nome) {
        if(nome.isEmpty()) throw new IllegalArgumentException();
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        if(cognome.isEmpty()) throw new IllegalArgumentException();
        this.cognome = cognome;
    }
}

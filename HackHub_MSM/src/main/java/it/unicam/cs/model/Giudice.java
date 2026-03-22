package it.unicam.cs.model;

import jakarta.persistence.Entity;
import lombok.Getter;

/**
 * Entità che rappresenta un Giudice nel sistema HackHub.
 */
@Getter
@Entity
public class Giudice extends MembroDelloStaff {

    public Giudice() {
        super();
    }

    public Giudice(String email, String nome, String cognome) {
        super(email, nome, cognome);
    }
}
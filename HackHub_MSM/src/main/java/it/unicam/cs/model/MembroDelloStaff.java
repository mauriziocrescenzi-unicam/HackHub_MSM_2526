package it.unicam.cs.model;

import jakarta.persistence.Entity;

/**
 * Classe che rappresenta un membro dello staff nel sistema HackHub.
 * Estende Account.
 */
@Entity
public abstract class MembroDelloStaff extends Account {

    public MembroDelloStaff() {
        super();
    }

    public MembroDelloStaff(String email, String nome, String cognome) {
        super(email, nome, cognome);
    }
}
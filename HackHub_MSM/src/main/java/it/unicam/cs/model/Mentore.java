package it.unicam.cs.model;

import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class Mentore extends MembroDelloStaff {

    public Mentore(String email, String nome, String cognome) {
        super(email, nome, cognome);
    }

    public Mentore() {

    }
}

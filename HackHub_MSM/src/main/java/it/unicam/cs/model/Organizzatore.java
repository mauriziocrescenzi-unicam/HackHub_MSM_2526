package it.unicam.cs.model;

import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class Organizzatore extends MembroDelloStaff {

    public Organizzatore(String email, String nome, String cognome) {
        super(email, nome, cognome);
    }

    public Organizzatore() {

    }
}


package it.unicam.cs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
public class Giudice extends Account {

    public Giudice() {
        super();
    }

    public Giudice(String email, String nome, String cognome) {
        super(email, nome, cognome);
    }
}

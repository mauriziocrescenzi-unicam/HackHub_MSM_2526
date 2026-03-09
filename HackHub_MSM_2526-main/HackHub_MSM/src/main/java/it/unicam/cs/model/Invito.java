package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
public class Invito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int idUtenteMittente;
    private int idUtenteDestinatario;
    private LocalDateTime dataInvio;
    @Enumerated(EnumType.STRING)
    private StatoInvito stato;

    public Invito() {}

    public Invito(int idUtenteMittente, int idUtenteDestinatario) {
        this.idUtenteMittente = idUtenteMittente;
        this.idUtenteDestinatario = idUtenteDestinatario;
        this.dataInvio = LocalDateTime.now();
        this.stato = StatoInvito.IN_ATTESA;
    }

    public void setDataInvio(LocalDateTime dataInvio) {
        if (dataInvio == null) throw new IllegalArgumentException();
        this.dataInvio = dataInvio;
    }

    public void setStato(StatoInvito stato) {
        if (stato == null) throw new IllegalArgumentException();
        this.stato = stato;
    }

    public void accettare() {
        if (this.stato != StatoInvito.IN_ATTESA) throw new IllegalArgumentException();
        this.stato = StatoInvito.ACCETTATO;
    }

    public void rifiutare() {
        if (this.stato != StatoInvito.IN_ATTESA) throw new IllegalArgumentException();
        this.stato = StatoInvito.RIFIUTATO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invito invito = (Invito) o;
        return id == invito.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Invito{id=" + id +
                ", idUtenteMittente=" + idUtenteMittente +
                ", idUtenteDestinatario=" + idUtenteDestinatario +
                ", dataInvio=" + dataInvio +
                ", stato=" + stato + "}";
    }
}
package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Objects;
/**
 * Entità che rappresenta un invito inviato da un membro di un team a un altro utente
 * per unirsi al team durante la fase di iscrizione o di conclusione di un hackathon.
 */
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
    /** Costruttore vuoto richiesto da JPA. */
    public Invito() {}
    /**
     * Costruttore per creare un nuovo invito.
     * La data di invio viene impostata automaticamente al momento corrente
     * e lo stato iniziale è {@link StatoInvito#IN_ATTESA}.
     *
     * @param idUtenteMittente     ID dell'utente che invia l'invito
     * @param idUtenteDestinatario ID dell'utente che riceve l'invito
     */
    public Invito(int idUtenteMittente, int idUtenteDestinatario) {
        this.idUtenteMittente = idUtenteMittente;
        this.idUtenteDestinatario = idUtenteDestinatario;
        this.dataInvio = LocalDateTime.now();
        this.stato = StatoInvito.IN_ATTESA;
    }
    /**
     * Imposta la data di invio dell'invito.
     *
     * @param dataInvio la data e ora di invio; non può essere {@code null}
     * @throws IllegalArgumentException se il valore è {@code null}
     */
    public void setDataInvio(LocalDateTime dataInvio) {
        if (dataInvio == null) throw new IllegalArgumentException();
        this.dataInvio = dataInvio;
    }
    /**
     * Imposta lo stato dell'invito.
     *
     * @param stato il nuovo stato; non può essere {@code null}
     * @throws IllegalArgumentException se il valore è {@code null}
     */
    public void setStato(StatoInvito stato) {
        if (stato == null) throw new IllegalArgumentException();
        this.stato = stato;
    }
    /**
     * Accetta l'invito, impostando lo stato a {@link StatoInvito#ACCETTATO}.
     * L'invito deve essere in stato {@link StatoInvito#IN_ATTESA}.
     *
     * @throws IllegalArgumentException se l'invito non è in stato {@code IN_ATTESA}
     */
    public void accettare() {
        if (this.stato != StatoInvito.IN_ATTESA) throw new IllegalArgumentException();
        this.stato = StatoInvito.ACCETTATO;
    }
    /**
     * Rifiuta l'invito, impostando lo stato a {@link StatoInvito#RIFIUTATO}.
     * L'invito deve essere in stato {@link StatoInvito#IN_ATTESA}.
     *
     * @throws IllegalArgumentException se l'invito non è in stato {@code IN_ATTESA}
     */
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
package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entità che rappresenta una richiesta di supporto da parte di un team
 * verso un mentore durante un hackathon.
 * <p>
 * Ogni richiesta contiene la descrizione del supporto richiesto,
 * la data di invio e, eventualmente, la descrizione della risposta
 * fornita dal mentore.
 * </p>
 */
@Entity
@Table(name = "richiesta_supporto")
@Getter
@Setter
public class RichiestaSupporto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descrizione_richiesta", nullable = false, length = 1000)
    private String descrizioneRichiesta;

    @Column(name = "data_invio", nullable = false)
    private LocalDateTime dataInvio;

    @Column(name = "descrizione_risposta", length = 500)
    private String descrizioneRisposta;

    public RichiestaSupporto() {}

    /**
     * Costruttore per creare una nuova richiesta di supporto.
     * Inizializza automaticamente la data di invio alla data corrente.
     *
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     */
    public RichiestaSupporto(String descrizioneRichiesta) {
        this.descrizioneRichiesta = descrizioneRichiesta;
        this.dataInvio = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RichiestaSupporto that = (RichiestaSupporto) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RichiestaSupporto{" +
                "id=" + id +
                ", descrizioneRichiesta='" + descrizioneRichiesta + '\'' +
                ", dataInvio=" + dataInvio +
                ", descrizioneRisposta='" + descrizioneRisposta + '\'' +
                '}';
    }
}
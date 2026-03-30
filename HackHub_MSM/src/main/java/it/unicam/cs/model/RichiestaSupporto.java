package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entità che rappresenta una richiesta di supporto da parte di un team
 * verso un mentore durante un hackathon.
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

    @Column(name = "id_team", nullable = false)
    private Long idTeam;

    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    public RichiestaSupporto() {}

    /**
     * Costruttore per creare una nuova richiesta di supporto.
     *
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     * @param idTeam               ID del team richiedente
     * @param dataInvio            Data e ora di invio
     * @param hackathon            Hackathon di riferimento
     */
    public RichiestaSupporto(String descrizioneRichiesta, Long idTeam,
                             LocalDateTime dataInvio, Hackathon hackathon) {
        this.descrizioneRichiesta = descrizioneRichiesta;
        this.idTeam = idTeam;
        this.dataInvio = dataInvio;
        this.hackathon = hackathon;
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
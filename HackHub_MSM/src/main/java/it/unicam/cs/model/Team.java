package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entità che rappresenta un Team partecipante agli hackathon.
 * Classe modello passiva: contiene solo dati e relazioni, senza logica di business.
 * La logica di gestione (creazione, iscrizioni, membri) è delegata al TeamController.
 *
 */
@Entity
@Getter
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_seq")
    @SequenceGenerator(name = "team_seq", sequenceName = "team_sequence", allocationSize = 1)
    private Long id;

    private String nome;
    private String descrizione;

    @Column(name = "data_creazione")
    private LocalDateTime dataCreazione;

    // ID dell'amministratore del team (riferimento a Utente)
    @Column(name = "amministratore_id")
    private Long amministratoreId;

    /**
     * Relazione uno-a-molti con TeamHackathon.
     * Cascade ALL: le operazioni sul team si propagano alle associazioni.
     */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamHackathon> hackathonIscritti;

    /**
     * Relazione uno-a-molti con MembroTeam.
     * Cascade ALL: le operazioni sul team si propagano ai membri.
     */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembroTeam> membri;

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public Team() {}

    /**
     * Costruttore per creare un nuovo team.
     * Imposta automaticamente la data di creazione a now().
     *
     * @param nome Nome del team
     * @param descrizione Descrizione del team
     * @param amministratoreId ID dell'utente che crea il team
     */
    public Team(String nome, String descrizione, Long amministratoreId) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.dataCreazione = LocalDateTime.now();
        this.amministratoreId = amministratoreId;
    }

    // ==================== SETTER ====================

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public void setAmministratoreId(Long amministratoreId) {
        this.amministratoreId = amministratoreId;
    }

    public void setHackathonIscritti(List<TeamHackathon> hackathonIscritti) {
        this.hackathonIscritti = hackathonIscritti;
    }

    public void setMembri(List<MembroTeam> membri) {
        this.membri = membri;
    }

    // ==================== METODI DI UTILITÀ ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", numeroMembri=" + (membri != null ? membri.size() : 0) +
                '}';
    }
}
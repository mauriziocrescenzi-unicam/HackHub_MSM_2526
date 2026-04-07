package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
@Setter
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
     */
    public Team(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.dataCreazione = LocalDateTime.now();
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
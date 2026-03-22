package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità che rappresenta la sottomissione di un Team per un Hackathon.
 * Esiste una sola sottomissione per team per hackathon.
 * Contiene la logica minima di validazione interna (core business).
 * La logica di gestione è delegata a SottomissioneService.
 */
@Getter
@Entity
public class Sottomissione {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sottomissione_seq")
    @SequenceGenerator(name = "sottomissione_seq", sequenceName = "sottomissione_sequence", allocationSize = 1)
    private Long id;

    private String nome;
    private String link;
    private LocalDateTime dataInvio;
    private String giudizio;
    private int voto;
    private Long idTeam;
    private Long idHackathon;

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public Sottomissione() {}

    /**
     * Costruttore per creare una nuova sottomissione.
     * Imposta automaticamente la data di invio a now().
     * Voto inizializzato a -1 (non ancora valutata).
     *
     * @param nome        Nome della sottomissione
     * @param link        Link al progetto (GitHub, Drive, ecc.)
     * @param idTeam      ID del team che invia la sottomissione
     * @param idHackathon ID dell'hackathon a cui è destinata
     */
    public Sottomissione(String nome, String link, Long idTeam, Long idHackathon) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome non valido.");
        if (link == null || link.isBlank()) throw new IllegalArgumentException("Link non valido.");
        if (idTeam == null || idTeam <= 0) throw new IllegalArgumentException("Team non valido.");
        if (idHackathon == null || idHackathon <= 0) throw new IllegalArgumentException("Hackathon non valido.");
        this.nome = nome;
        this.link = link;
        this.idTeam = idTeam;
        this.idHackathon = idHackathon;
        this.dataInvio = LocalDateTime.now();
        this.voto = -1;
        this.giudizio = null;
    }


    /**
     * Aggiorna nome e link della sottomissione aggiornando la data di invio a now().
     * Corrisponde a setInfo(nome, link, now()) del sequence diagram aggiornare.
     *
     * @param nome Nuovo nome della sottomissione
     * @param link Nuovo link al progetto
     */
    public void setInfo(String nome, String link) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome non valido.");
        if (link == null || link.isBlank()) throw new IllegalArgumentException("Link non valido.");
        this.nome = nome;
        this.link = link;
        this.dataInvio = LocalDateTime.now();
    }

    /**
     * Imposta la valutazione della sottomissione.
     * Valida internamente che il voto sia tra 0 e 10 e che il giudizio non sia vuoto.
     * Corrisponde a setValutazione(voto, descrizione) del sequence diagram valutare.
     *
     * @param voto     Punteggio numerico (0-10)
     * @param giudizio Giudizio scritto del giudice
     */
    public void setValutazione(int voto, String giudizio) {
        if (voto < 0 || voto > 10) throw new IllegalArgumentException("Voto deve essere compreso tra 0 e 10.");
        if (giudizio == null || giudizio.isBlank()) throw new IllegalArgumentException("Giudizio non valido.");
        this.voto = voto;
        this.giudizio = giudizio;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sottomissione that = (Sottomissione) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sottomissione{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", link='" + link + '\'' +
                ", dataInvio=" + dataInvio +
                ", voto=" + voto +
                '}';
    }
}
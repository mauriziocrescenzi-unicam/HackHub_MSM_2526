package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Entità che rappresenta un hackathon nel sistema HackHub.
 */
@Getter
@Entity
public class Hackathon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String regolamento;
    private LocalDateTime scadenzaIscrizione;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;
    private String luogo;
    private Double premioInDenaro;
    private Integer dimensioneMassimoTeam;
    @Enumerated(EnumType.STRING)
    private StatoHackathon stato;
    @ManyToOne
    @JoinColumn(name = "organizzatore_id")
    private Account organizzatore;
    @ManyToOne
    @JoinColumn(name = "giudice_id")
    private Account giudice;
    @ManyToMany
    @JoinTable(
        name = "hackathon_mentori",
        joinColumns = @JoinColumn(name = "hackathon_id"),
        inverseJoinColumns = @JoinColumn(name = "mentore_id")
    )
    private List<Account> mentori;

    @ManyToOne
    @JoinColumn(name = "team_vincitore_id")
    private Team teamVincitore;
    /** Costruttore vuoto richiesto da JPA. */
    public Hackathon() {

    }
    /**
     * Imposta il nome dell'hackathon.
     *
     * @param nome il nome dell'hackathon; non può essere vuoto
     * @throws IllegalArgumentException se il nome è vuoto
     */
    public void setNome( String nome) {
        if(nome.isEmpty()) throw new IllegalArgumentException();
        this.nome = nome;
    }
    /**
     * Imposta il regolamento dell'hackathon.
     *
     * @param regolamento il testo del regolamento; non può essere vuoto
     * @throws IllegalArgumentException se il regolamento è vuoto
     */
    public void setRegolamento( String regolamento) {
        if(regolamento.isEmpty()) throw new IllegalArgumentException();
        this.regolamento = regolamento;
    }
    /**
     * Imposta la data di scadenza delle iscrizioni.
     *
     * @param scadenzaIscrizione la data di scadenza; non può essere {@code null}
     * @throws IllegalArgumentException se il valore è {@code null}
     */
    public void setScadenzaIscrizione( LocalDateTime scadenzaIscrizione) {
        if(scadenzaIscrizione==null) throw new IllegalArgumentException();
        this.scadenzaIscrizione = scadenzaIscrizione;
    }
    /**
     * Imposta la data di inizio dell'hackathon.
     *
     * @param dataInizio la data di inizio; non può essere {@code null}
     * @throws IllegalArgumentException se il valore è {@code null}
     */
    public void setDataInizio( LocalDateTime dataInizio) {
        if(dataInizio==null) throw new IllegalArgumentException();
        this.dataInizio = dataInizio;
    }
    /**
     * Imposta la data di fine dell'hackathon.
     *
     * @param dataFine la data di fine; non può essere {@code null}
     * @throws IllegalArgumentException se il valore è {@code null}
     */
    public void setDataFine( LocalDateTime dataFine) {
        if(dataFine==null) throw new IllegalArgumentException();
        this.dataFine = dataFine;
    }

    /**
     * Imposta il luogo in cui si svolge l'hackathon.
     *
     * @param luogo il luogo dell'evento; non può essere vuoto
     * @throws IllegalArgumentException se il luogo è vuoto
     */
    public void setLuogo(String luogo) {
        if(luogo.isEmpty()) throw new IllegalArgumentException();
        this.luogo = luogo;
    }
    /**
     * Imposta il premio in denaro per i vincitori.
     *
     * @param premioInDenaro il valore del premio; deve essere maggiore o uguale a zero
     * @throws IllegalArgumentException se il valore è negativo
     */
    public void setPremioInDenaro(double premioInDenaro) {
        if (premioInDenaro < 0) throw new IllegalArgumentException();
        this.premioInDenaro = premioInDenaro;
    }
    /**
     * Imposta la dimensione massima consentita per ogni team.
     *
     * @param dimensioneMassimoTeam il numero massimo di membri per team; deve essere maggiore di zero
     * @throws IllegalArgumentException se il valore è minore o uguale a zero
     */
    public void setDimensioneMassimoTeam(int dimensioneMassimoTeam) {
        if (dimensioneMassimoTeam <= 0) throw new IllegalArgumentException();
        this.dimensioneMassimoTeam = dimensioneMassimoTeam;
    }

    /**
     * Imposta lo stato corrente dell'hackathon senza validare la transizione.
     * Per transizioni validate usare {@link #cambiaStato(StatoHackathon)}.
     *
     * @param stato il nuovo stato; non può essere {@code null}
     * @throws IllegalArgumentException se lo stato è {@code null}
     */
    public void setStato(StatoHackathon stato) {
        if (stato == null) throw new IllegalArgumentException();
        this.stato = stato;
    }
    /**
     * Esegue una transizione di stato
     *
     * @param nuovoStato il nuovo stato a cui transitare
     * @throws IllegalStateException se la transizione dal stato corrente al nuovo stato non è consentita
     */
    public void cambiaStato(StatoHackathon nuovoStato) {
        if (!transazioneValida(this.stato, nuovoStato))
            throw new IllegalStateException("Transizione non consentita: " + this.stato + " a " + nuovoStato);
        this.stato = nuovoStato;
    }
    /**
     * Verifica se la transizione tra due stati è consentita.
     *
     * @param from lo stato di partenza
     * @param to   lo stato di destinazione
     * @return {@code true} se la transizione è valida, {@code false} altrimenti
     */
    private boolean transazioneValida(StatoHackathon from, StatoHackathon to) {
        return switch (from) {
            case IN_ISCRIZIONE   -> to == StatoHackathon.IN_CORSO;
            case IN_CORSO        -> to == StatoHackathon.IN_VALUTAZIONE;
            case IN_VALUTAZIONE  -> to == StatoHackathon.CONCLUSO;
            case CONCLUSO        -> false;
        };
    }
    /**
     * Imposta l'organizzatore dell'hackathon.
     *
     * @param organizzatore l'account dell'organizzatore; non può essere {@code null}
     * @throws IllegalArgumentException se l'organizzatore è {@code null}
     */
    public void setOrganizzatore(Account organizzatore) {
        if (organizzatore == null) throw new IllegalArgumentException();
        this.organizzatore = organizzatore;
    }
    /**
     * Imposta il giudice dell'hackathon.
     *
     * @param giudice l'account del giudice; non può essere {@code null}
     * @throws IllegalArgumentException se il giudice è {@code null}
     */
    public void setGiudice(Account giudice) {
        if (giudice == null) throw new IllegalArgumentException();
        this.giudice = giudice;
    }
    /**
     * Imposta la lista dei mentori assegnati all'hackathon.
     *
     * @param mentori la lista dei mentori; non può essere {@code null} o vuota
     * @throws IllegalArgumentException se la lista è {@code null} o vuota
     */
    public void setMentori(List<Account> mentori) {
        if (mentori ==null ||mentori.isEmpty()) throw new IllegalArgumentException();
        this.mentori = mentori;
    }

    /**
     * Imposta il team vincitore dell'hackathon.
     *
     * @param teamVincitore Team vincitore da impostare, o null per rimuovere il vincitore
     * @throws IllegalArgumentException se il team è null e l'hackathon è già concluso
     */
    public void setVincitore(Team teamVincitore) {
        // Se l'hackathon è concluso, deve avere un vincitore
        if (this.stato == StatoHackathon.CONCLUSO && teamVincitore == null) {
            throw new IllegalArgumentException("Un hackathon concluso deve avere un team vincitore");
        }

        // Se si imposta un vincitore, l'hackathon dovrebbe essere in valutazione o concluso
        if (teamVincitore != null &&
                this.stato != StatoHackathon.IN_VALUTAZIONE &&
                this.stato != StatoHackathon.CONCLUSO) {
            throw new IllegalArgumentException("Può impostare un vincitore solo per hackathon in valutazione o conclusi");
        }

        this.teamVincitore = teamVincitore;
    }
}

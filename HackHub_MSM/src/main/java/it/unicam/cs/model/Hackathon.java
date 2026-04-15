package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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

    public Hackathon() {

    }

    public void setNome( String nome) {
        if(nome.isEmpty()) throw new IllegalArgumentException();
        this.nome = nome;
    }

    public void setRegolamento( String regolamento) {
        if(regolamento.isEmpty()) throw new IllegalArgumentException();
        this.regolamento = regolamento;
    }

    public void setScadenzaIscrizione( LocalDateTime scadenzaIscrizione) {
        if(scadenzaIscrizione==null) throw new IllegalArgumentException();
        this.scadenzaIscrizione = scadenzaIscrizione;
    }

    public void setDataInizio( LocalDateTime dataInizio) {
        if(dataInizio==null) throw new IllegalArgumentException();
        this.dataInizio = dataInizio;
    }

    public void setDataFine( LocalDateTime dataFine) {
        if(dataFine==null) throw new IllegalArgumentException();
        this.dataFine = dataFine;
    }

    public void setLuogo(String luogo) {
        if(luogo.isEmpty()) throw new IllegalArgumentException();
        this.luogo = luogo;
    }

    public void setPremioInDenaro(double premioInDenaro) {
        if (premioInDenaro < 0) throw new IllegalArgumentException();
        this.premioInDenaro = premioInDenaro;
    }

    public void setDimensioneMassimoTeam(int dimensioneMassimoTeam) {
        if (dimensioneMassimoTeam <= 0) throw new IllegalArgumentException();
        this.dimensioneMassimoTeam = dimensioneMassimoTeam;
    }

    public void setStato(StatoHackathon stato) {
        if (stato == null) throw new IllegalArgumentException();
        this.stato = stato;
    }

    public void setOrganizzatore(Account organizzatore) {
        if (organizzatore == null) throw new IllegalArgumentException();
        this.organizzatore = organizzatore;
    }

    public void setGiudice(Account giudice) {
        if (giudice == null) throw new IllegalArgumentException();
        this.giudice = giudice;
    }

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

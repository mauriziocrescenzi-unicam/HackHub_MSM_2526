package it.unicam.cs.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.random.RandomGenerator;

@Getter
@Entity
public class Hackathon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
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
    private Organizzatore organizzatore;
    @ManyToOne
    @JoinColumn(name = "giudice_id")
    private Giudice giudice;
    @ManyToMany
    @JoinTable(
        name = "hackathon_mentori",
        joinColumns = @JoinColumn(name = "hackathon_id"),
        inverseJoinColumns = @JoinColumn(name = "mentore_id")
    )
    private List<Mentore> mentori;

    public Hackathon() {
        id = RandomGenerator.getDefault().nextInt();
    }

    public void setNome( String nome) {
        if(nome.isEmpty()) throw new InputMismatchException();
        this.nome = nome;
    }

    public void setRegolamento( String regolamento) {
        if(regolamento.isEmpty()) throw new InputMismatchException();
        this.regolamento = regolamento;
    }

    public void setScadenzaIscrizione( LocalDateTime scadenzaIscrizione) {
        if(scadenzaIscrizione==null) throw new InputMismatchException();
        this.scadenzaIscrizione = scadenzaIscrizione;
    }

    public void setDataInizio( LocalDateTime dataInizio) {
        if(dataInizio==null) throw new InputMismatchException();
        this.dataInizio = dataInizio;
    }

    public void setDataFine( LocalDateTime dataFine) {
        if(dataFine==null) throw new InputMismatchException();
        this.dataFine = dataFine;
    }

    public void setLuogo(String luogo) {
        if(luogo.isEmpty()) throw new InputMismatchException();
        this.luogo = luogo;
    }

    public void setPremioInDenaro(double premioInDenaro) {
        if (premioInDenaro < 0) throw new InputMismatchException();
        this.premioInDenaro = premioInDenaro;
    }

    public void setDimensioneMassimoTeam(int dimensioneMassimoTeam) {
        if (dimensioneMassimoTeam <= 0) throw new InputMismatchException();
        this.dimensioneMassimoTeam = dimensioneMassimoTeam;
    }

    public void setStato(StatoHackathon stato) {
        if (stato == null) throw new InputMismatchException();
        this.stato = stato;
    }

    public void setOrganizzatore(Organizzatore organizzatore) {
        if (organizzatore == null) throw new InputMismatchException();
        this.organizzatore = organizzatore;
    }

    public void setGiudice(Giudice giudice) {
        if (giudice == null) throw new InputMismatchException();
        this.giudice = giudice;
    }

    public void setMentori(List<Mentore> mentori) {
        if (mentori ==null ||mentori.isEmpty()) throw new InputMismatchException();
        this.mentori = mentori;
    }
}

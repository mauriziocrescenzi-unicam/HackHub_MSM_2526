package it.unicam.cs.Controller;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.time.LocalDateTime;
import java.util.List;

public class HackathonController {
    private static HackathonController controller;
    private final GiudiceController giudiceController = GiudiceController.getInstance();
    private final Builder builder;
    private final StandardPersistence<Hackathon> persistence;

    public static HackathonController getInstance(){
        if(controller == null) controller = new HackathonController(new HackathonBuilder());
        return controller;
    }
    HackathonController(Builder builder) {
        this.persistence = new StandardPersistence<>(Hackathon.class);
        this.builder = builder;
    }
    public boolean creaHackathon(String nome, String regolamento, LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio,
                              LocalDateTime dataFine, String luogo, double premioInDenaro, int dimensioneMassimoTeam,
                              StatoHackathon stato, Organizzatore organizzatore, Giudice giudice, List<Mentore> mentori){
        if(!verificaRequisiti(scadenzaIscrizione,dataInizio,dataFine)) return false;
        builder.reset();
        builder.setInfo(nome,regolamento,scadenzaIscrizione,dataInizio,dataFine,luogo,premioInDenaro,dimensioneMassimoTeam,stato,organizzatore);
        builder.setGiudice(giudice);
        builder.setMentori(mentori);
        Hackathon hackathon = builder.getResult();
        persistence.create(hackathon);
        return hackathon != null;
    }

    /**
     * Verifica che le date siano valide.
     */
    public boolean verificaRequisiti( LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio, LocalDateTime dataFine){
        if(scadenzaIscrizione.isBefore(LocalDateTime.now()) || dataInizio.isBefore(LocalDateTime.now())
                || dataFine.isBefore(LocalDateTime.now())) return false;
        if (scadenzaIscrizione.isAfter(dataInizio)) return false;
        if (dataInizio.isAfter(dataFine)) return false;
        return true;
    }

    /**
     * Restituisce lo stato del hackathon.
     */
    public StatoHackathon getStatoHackathon(Hackathon hackathon){
        return hackathon.getStato();
    }

}

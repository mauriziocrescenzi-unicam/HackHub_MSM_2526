package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.time.LocalDateTime;
import java.util.List;

public class HackathonService {
    private static HackathonService service;
    private final GiudiceService giudiceService = GiudiceService.getInstance();
    private final Builder builder;
    private final StandardPersistence<Hackathon> persistence;

    public static HackathonService getInstance(){
        if(service == null) service = new HackathonService(new HackathonBuilder());
        return service;
    }
    HackathonService(Builder builder) {
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


    public Hackathon getHackathonByID(long idHackathon) {
        return persistence.findById(idHackathon);
    }

    public StatoHackathon getStatoHackathon(Hackathon hackathon) {
        return hackathon.getStato();
    }
}

package it.unicam.cs.Controller;

import it.unicam.cs.model.*;

import java.time.LocalDateTime;
import java.util.List;

public class HackathonController {
    Builder builder;
    public HackathonController(Builder builder) {
        this.builder = builder;
    }
    public boolean creaHackathon(String nome, String regolamento, LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio,
                              LocalDateTime dataFine, String luogo, double premioInDenaro, int dimensioneMassimoTeam,
                              StatoHackathon stato, Organizzatore organizzatore, Giudice giudice, List<Mentore> mentori){
        if(!verificaRequisiti(nome,regolamento,scadenzaIscrizione,dataInizio,dataFine,luogo,premioInDenaro,dimensioneMassimoTeam,stato,organizzatore,giudice,mentori)) return false;
        List<Giudice> giudici=GiudiceController.getListaGiudici();




        return true;
    }
    public boolean verificaRequisiti(String nome, String regolamento, LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio,
                                     LocalDateTime dataFine, String luogo, double premioInDenaro, int dimensioneMassimoTeam,
                                     StatoHackathon stato, Organizzatore organizzatore, Giudice giudice, List<Mentore> mentori){
        if (nome.isEmpty() || regolamento.isEmpty() || scadenzaIscrizione.isBefore(LocalDateTime.now())
                || dataInizio.isBefore(LocalDateTime.now()) || dataFine.isBefore(LocalDateTime.now())
                || luogo.isEmpty() || premioInDenaro <= 0 || dimensioneMassimoTeam <= 0 || stato == null
                || organizzatore == null || giudice == null || mentori == null)
            return false;
        return true;
    }
    public StatoHackathon getStatoHackathon(Hackathon hackathon){
        return hackathon.getStato();
    }

}

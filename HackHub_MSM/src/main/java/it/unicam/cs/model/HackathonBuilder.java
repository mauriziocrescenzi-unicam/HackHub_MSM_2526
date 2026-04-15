package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.List;

public class HackathonBuilder implements Builder{
    Hackathon hackathon;
    public HackathonBuilder(){}
    @Override
    public void reset() {
        hackathon = null;
    }

    @Override
    public void setGiudice(Account giudice) {
        if(hackathon == null) throw new IllegalStateException("Hackathon non creato");
        hackathon.setGiudice(giudice);
    }

    @Override
    public void setMentori(List<Account> mentori) {
        if(hackathon == null) throw new IllegalStateException("Hackathon non creato");
        hackathon.setMentori(mentori);
    }

    @Override
    public void setInfo(String nome, String regolamento,LocalDateTime scadenzaIscrizioni,LocalDateTime dataInizio, LocalDateTime dataFine,
                        String luogo, double premio, int dimensione, StatoHackathon stato,Account organizzatore) {
        hackathon= new Hackathon();
        hackathon.setNome(nome);
        hackathon.setRegolamento(regolamento);
        hackathon.setScadenzaIscrizione(scadenzaIscrizioni);
        hackathon.setDataInizio(dataInizio);
        hackathon.setDataFine(dataFine);
        hackathon.setLuogo(luogo);
        hackathon.setPremioInDenaro(premio);
        hackathon.setDimensioneMassimoTeam(dimensione);
        hackathon.setStato(stato);
        hackathon.setOrganizzatore(organizzatore);
    }

    @Override
    public Hackathon getResult() {
        if(hackathon == null) throw new IllegalStateException("Hackathon non creato");
        return hackathon;
    }
}

package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.List;

public class HackathonBuilder implements Builder{
    Hackathon hackathon;
    public HackathonBuilder(){}
    @Override
    public void reset() {

    }

    @Override
    public void setGiudice(Giudice giudice) {

    }

    @Override
    public void setMentori(List<Mentore> mentori) {

    }

    @Override
    public void setInfo(String nome, String regolamento, LocalDateTime dataInizio, LocalDateTime dataFine,
                        String luogo, double premio, int dimensione, StatoHackathon stato,Organizzatore organizzatore) {

    }

    @Override
    public Hackathon getResult() {
        return hackathon;
    }
}

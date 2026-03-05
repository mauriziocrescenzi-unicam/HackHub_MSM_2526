package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.List;

public interface Builder {
    void reset();
    void setGiudice(Giudice giudice);
    void setMentori(List<Mentore> mentori);
    void setInfo(String nome, String regolamento,LocalDateTime scadenzaIscrizioni, LocalDateTime dataInizio, LocalDateTime dataFine,
                 String luogo, double premio, int dimensione, StatoHackathon stato, Organizzatore organizzatore);
    Hackathon getResult();
    }

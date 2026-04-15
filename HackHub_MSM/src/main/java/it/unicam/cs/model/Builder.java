package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.List;

public interface Builder {
    void reset();
    void setGiudice(Account giudice);
    void setMentori(List<Account> mentori);
    void setInfo(String nome, String regolamento,LocalDateTime scadenzaIscrizioni, LocalDateTime dataInizio, LocalDateTime dataFine,
                 String luogo, double premio, int dimensione, StatoHackathon stato, Account organizzatore);
    Hackathon getResult();
    }

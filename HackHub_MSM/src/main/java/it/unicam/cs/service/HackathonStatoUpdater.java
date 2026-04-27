package it.unicam.cs.service;

import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.repository.HackathonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class HackathonStatoUpdater {

    private final HackathonRepository repository;

    public HackathonStatoUpdater(HackathonRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void aggiornaStati() {
        LocalDateTime ora = LocalDateTime.now();

        repository.findByStato(StatoHackathon.IN_ISCRIZIONE).stream()
                .filter(h -> !h.getDataInizio().isAfter(ora))
                .forEach(h -> {
                    h.cambiaStato(StatoHackathon.IN_CORSO);
                    repository.save(h);
                });

        repository.findByStato(StatoHackathon.IN_CORSO).stream()
                .filter(h -> !h.getDataFine().isAfter(ora))
                .forEach(h -> {
                    h.cambiaStato(StatoHackathon.IN_VALUTAZIONE);
                    repository.save(h);
                });
    }
}
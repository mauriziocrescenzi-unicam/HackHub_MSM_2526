package it.unicam.cs.service;

import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.repository.HackathonRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class HackathonScheduler {

    private final HackathonRepository repository;

    public HackathonScheduler(HackathonRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class) // esegue subito all'avvio
    @Scheduled(fixedRate = 60000)               // poi ogni minuto
    @Transactional
    public void aggiornaStati() {
        LocalDateTime ora = LocalDateTime.now();

        // IN_ISCRIZIONE → IN_CORSO
        repository.findByStato(StatoHackathon.IN_ISCRIZIONE).stream()
                .filter(h -> !h.getDataInizio().isAfter(ora))
                .forEach(h -> h.cambiaStato(StatoHackathon.IN_CORSO));

        // IN_CORSO → IN_VALUTAZIONE
        repository.findByStato(StatoHackathon.IN_CORSO).stream()
                .filter(h -> !h.getDataFine().isAfter(ora))
                .forEach(h -> h.cambiaStato(StatoHackathon.IN_VALUTAZIONE));
    }
}
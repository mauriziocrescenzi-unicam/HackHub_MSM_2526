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

    private final HackathonStatoUpdater updater;

    public HackathonScheduler(HackathonStatoUpdater updater) {
        this.updater = updater;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aggiornaStatiAllAvvio() {
        updater.aggiornaStati();
    }

    @Scheduled(fixedRate = 60000)
    public void aggiornaStatiScheduled() {
        updater.aggiornaStati();
    }
}
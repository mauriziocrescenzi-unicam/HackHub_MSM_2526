package it.unicam.cs.scheduler;

import it.unicam.cs.service.HackathonStatoUpdater;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Componente scheduler per l'aggiornamento periodico degli stati degli hackathon.
 * Esegue l'aggiornamento al momento dell'avvio dell'applicazione e successivamente
 * ogni 60 secondi.
 */
@Component
public class HackathonScheduler {

    private final HackathonStatoUpdater updater;
    /**
     * Costruisce un'istanza di {@code HackathonScheduler} con il componente di aggiornamento.
     *
     * @param updater il componente responsabile dell'aggiornamento degli stati
     */
    public HackathonScheduler(HackathonStatoUpdater updater) {
        this.updater = updater;
    }
    /**
     * Aggiorna gli stati di tutti gli hackathon al momento del completamento
     * dell'avvio dell'applicazione.
     * Viene eseguito una sola volta, in risposta all'evento {@link ApplicationReadyEvent}.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void aggiornaStatiAllAvvio() {
        updater.aggiornaStati();
    }
    /**
     * Aggiorna periodicamente gli stati di tutti gli hackathon.
     * Viene eseguito ogni 60.000 millisecondi (1 minuto).
     */
    @Scheduled(fixedRate = 60000)
    public void aggiornaStatiScheduled() {
        updater.aggiornaStati();
    }
}
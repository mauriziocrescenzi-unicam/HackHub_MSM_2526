package it.unicam.cs.service;

import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.repository.HackathonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
/**
 * Componente responsabile dell'aggiornamento automatico degli stati degli hackathon.
 * Applica le transizioni di stato in base alle date correnti:
 * <ul>
 *   <li>{@link StatoHackathon#IN_ISCRIZIONE} → {@link StatoHackathon#IN_CORSO} quando la data di inizio è raggiunta</li>
 *   <li>{@link StatoHackathon#IN_CORSO} → {@link StatoHackathon#IN_VALUTAZIONE} quando la data di fine è raggiunta</li>
 * </ul>
 */
@Component
public class HackathonStatoUpdater {

    private final HackathonRepository repository;
    /**
     * Costruisce un'istanza di {@code HackathonStatoUpdater} con il repository degli hackathon.
     *
     * @param repository repository per l'accesso e il salvataggio degli hackathon
     */
    public HackathonStatoUpdater(HackathonRepository repository) {
        this.repository = repository;
    }
    /**
     * Aggiorna lo stato di tutti gli hackathon in base al momento corrente.
     * Ogni hackathon aggiornato viene salvato nel repository.
     */
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
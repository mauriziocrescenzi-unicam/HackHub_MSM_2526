package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.AccountRepository;
import it.unicam.cs.repository.HackathonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsabile della gestione dei Giudici nel sistema HackHub.
 * Implementa il pattern Singleton.
 * Gestisce il caso d'uso: valutare sottomissioni.
 */
@Service
@Transactional
public class GiudiceService {

    private static GiudiceService service;

    private final AccountRepository repository;
    private final HackathonRepository hackathonRepository;
    private final SottomissioneService sottomissioneService;

    public GiudiceService(AccountRepository repository,
                          HackathonService hackathonService,
                          SottomissioneService sottomissioneService, HackathonRepository hackathonRepository) {
        this.repository = repository;
        this.hackathonRepository = hackathonRepository;
        this.sottomissioneService = sottomissioneService;
    }

    public List<Account> getListaGiudici() {
        return repository.findAll();
    }

    public boolean verificaGiudice(Account giudice) {
        if (giudice == null) return false;
        return repository.findById(giudice.getId()).isPresent();
    }

    /**
     * Restituisce la lista degli hackathon assegnati a un giudice filtrati per stato.
     */
    public List<Hackathon> getListaHackathon(StatoHackathon stato, Long idGiudice) {
        if (stato == null) throw new IllegalArgumentException("Stato non valido.");
        if (idGiudice == null || idGiudice <= 0) throw new IllegalArgumentException("Giudice non valido.");

        Account giudice = repository.findById(idGiudice).orElse(null);
        if (giudice == null) throw new IllegalArgumentException("Giudice non trovato.");

        // Recupera tutti gli hackathon e filtra per giudice assegnato e stato
        return hackathonRepository.findAll().stream()
                .filter(h -> h.getGiudice() != null)
                .filter(h -> h.getGiudice().getId().equals(idGiudice))
                .filter(h -> h.getStato() == stato)
                .toList();
    }

    /**
     * Restituisce la lista delle sottomissioni per un hackathon.
     */
    public List<Sottomissione> getSottomissioni(Hackathon hackathon) {
        return sottomissioneService.getSottomissioni(hackathon);
    }

    /**
     * Verifica se una sottomissione è già stata valutata.
     */
    public boolean isSottomissioneValutata(Sottomissione sottomissione) {
        return sottomissioneService.isSottomissioneValutata(sottomissione);
    }

    /**
     * Valuta una sottomissione assegnando voto e giudizio.
     */
    public boolean valutaSottomissione(Sottomissione sottomissione, int voto, String giudizio) {
        return sottomissioneService.valutaSottomissione(sottomissione, voto, giudizio);
    }

    public Account getGiudiceById(Long idGiudice) {
        if (idGiudice == null || idGiudice <= 0) {
            return null;
        }
        return repository.findById(idGiudice).orElse(null);
    }
}
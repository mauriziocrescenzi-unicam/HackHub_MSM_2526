package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsabile della gestione dei Giudici nel sistema HackHub.
 * Implementa il pattern Singleton.
 * Gestisce il caso d'uso: valutare sottomissioni.
 */
public class GiudiceService {

    private static GiudiceService service;

    private final StandardPersistence<Giudice> persistence;
    private final MembroDelloStaffService membroStaffService;
    private final SottomissioneService sottomissioneService;

    private GiudiceService() {
        this.persistence = new StandardPersistence<>(Giudice.class);
        this.membroStaffService = MembroDelloStaffService.getInstance();
        this.sottomissioneService = SottomissioneService.getInstance();
    }

    public static GiudiceService getInstance() {
        if (service == null) service = new GiudiceService();
        return service;
    }

    public List<Giudice> getListaGiudici() {
        return persistence.getAll();
    }

    public boolean verificaGiudice(Giudice giudice) {
        if (giudice == null) return false;
        return persistence.existsById(giudice.getId());
    }

    /**
     * Restituisce la lista degli hackathon assegnati a un giudice filtrati per stato.
     */
    public List<Hackathon> getListaHackathon(StatoHackathon stato, Long idGiudice) {
        if (stato == null) throw new IllegalArgumentException("Stato non valido.");
        if (idGiudice == null || idGiudice <= 0) throw new IllegalArgumentException("Giudice non valido.");

        Giudice giudice = persistence.findById(idGiudice);
        if (giudice == null) throw new IllegalArgumentException("Giudice non trovato.");

        List<Hackathon> assegnati = membroStaffService.getListaHackathon(giudice);
        List<Hackathon> risultato = new ArrayList<>();
        for (Hackathon h : assegnati) {
            if (h.getStato() == stato) {
                risultato.add(h);
            }
        }
        return risultato;
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
}
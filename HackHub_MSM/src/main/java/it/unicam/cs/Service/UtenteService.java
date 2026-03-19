package it.unicam.cs.Service;

import it.unicam.cs.model.Utente;
import it.unicam.cs.persistence.StandardPersistence;
/**
 * Gestisce la logica relativa agli utenti
 */
public class UtenteService {

    private static UtenteService instance;

    private final StandardPersistence<Utente> persistence;
    private final MembroTeamService membroTeamService;

    private UtenteService(MembroTeamService membroTeamService) {
        this.persistence = new StandardPersistence<>(Utente.class);
        this.membroTeamService = membroTeamService;
    }

    public static UtenteService getInstance(MembroTeamService membroTeamService) {
        if (instance == null)
            instance = new UtenteService(membroTeamService);
        return instance;
    }

    public boolean isPresent(Utente utente) {
        if (utente == null) return false;
        return persistence.findById(utente.getId()) != null;
    }

    public boolean isMembroTeam(Utente utente) {
        return membroTeamService.isMembroTeam(utente);
    }

    /**
     * Restituisce l'utente dato il suo id
     */
    public Utente findById(Long id) {
        if (id == null) throw new IllegalArgumentException();
        return persistence.findById(id);
    }
}
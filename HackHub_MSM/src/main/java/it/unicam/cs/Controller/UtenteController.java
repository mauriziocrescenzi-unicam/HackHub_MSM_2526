package it.unicam.cs.Controller;

import it.unicam.cs.model.Utente;
import it.unicam.cs.persistence.StandardPersistence;
/**
 * Gestisce la logica relativa agli utenti
 */
public class UtenteController {

    private static UtenteController instance;

    private final StandardPersistence<Utente> persistence;
    private final MembroTeamController membroTeamController;

    private UtenteController(MembroTeamController membroTeamController) {
        this.persistence = new StandardPersistence<>(Utente.class);
        this.membroTeamController = membroTeamController;
    }

    public static UtenteController getInstance(MembroTeamController membroTeamController) {
        if (instance == null)
            instance = new UtenteController(membroTeamController);
        return instance;
    }

    public boolean isPresent(Utente utente) {
        if (utente == null) return false;
        return persistence.findById(utente.getId()) != null;
    }

    public boolean isMembroTeam(Utente utente) {
        return membroTeamController.isMembroTeam(utente);
    }

    /**
     * Restituisce l'utente dato il suo id
     */
    public Utente findById(Long id) {
        if (id == null) throw new IllegalArgumentException();
        return persistence.findById(id);
    }
}
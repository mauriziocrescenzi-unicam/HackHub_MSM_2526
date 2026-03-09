package it.unicam.cs.Controller;

import it.unicam.cs.model.Utente;
import it.unicam.cs.persistence.StandardPersistence;

public class UtenteController {

    private final StandardPersistence<Utente> persistence;
    private final MembroTeamController membroTeamController;

    public UtenteController(MembroTeamController membroTeamController) {
        this.persistence = new StandardPersistence<>(Utente.class);
        this.membroTeamController = membroTeamController;
    }

    /**
     * Verifica che l'utente esista nella piattaforma tramite DB.
     */
    public boolean isPresent(Utente utente) {
        if (utente == null) return false;
        return persistence.findById(utente.getId()) != null;
    }

    /**
     * Verifica che l'utente sia già membro di un team.
     * Delega a MembroTeamController.
     */
    public boolean isMembroTeam(Utente utente) {
        return membroTeamController.isMembroTeam(utente);
    }

    /**
     * Recupera un utente dal DB tramite il suo id.
     */
    public Utente findById(Long id) {
        if (id == null) throw new IllegalArgumentException();
        return persistence.findById(id);
    }
}
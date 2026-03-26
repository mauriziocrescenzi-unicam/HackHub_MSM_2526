package it.unicam.cs.service;

import it.unicam.cs.model.Utente;
import it.unicam.cs.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestisce la logica relativa agli utenti
 */
@Service
@Transactional
public class UtenteService {

    private final UtenteRepository repository;
    private final MembroTeamService membroTeamService;

    public UtenteService(UtenteRepository repository, MembroTeamService membroTeamService) {
        this.repository = repository;
        this.membroTeamService = membroTeamService;
    }



    public boolean isPresent(Utente utente) {
        if (utente == null) return false;
        return repository.findById(utente.getId()).isPresent();
    }

    public boolean isMembroTeam(Utente utente) {
        return membroTeamService.isMembroTeam(utente);
    }

    /**
     * Restituisce l'utente dato il suo id
     */
    public Utente findById(Long id) {
        if (id == null) throw new IllegalArgumentException();
        return repository.findById(id).orElse(null);
    }
}
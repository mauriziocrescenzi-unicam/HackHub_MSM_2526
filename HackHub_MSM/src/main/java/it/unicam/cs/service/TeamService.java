package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.HackathonRepository;
import it.unicam.cs.repository.MembroTeamRepository;
import it.unicam.cs.repository.TeamHackathonRepository;
import it.unicam.cs.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsabile della gestione dei Team nel sistema HackHub.
 * Implementa il pattern Singleton per garantire un'unica istanza del controller.
 * Contiene tutta la logica di business per operazioni sui team.
 *
 */
@Service
@Transactional
public class TeamService {

    private final TeamRepository repository;
    private final HackathonRepository hackathonRepository;
    private final TeamHackathonRepository teamHackathonRepository;
    private final MembroTeamService membroTeamService;
    private final MembroTeamRepository membroTeamRepository;
    private final HackathonService hackathonService;
    private final AccountService accountService;

    public TeamService(TeamRepository repository, HackathonRepository hackathonRepository, TeamHackathonRepository teamHackathonRepository, MembroTeamService membroTeamService, MembroTeamRepository membroTeamRepository, HackathonService hackathonService,AccountService accountService) {
        this.repository = repository;
        this.hackathonRepository = hackathonRepository;
        this.teamHackathonRepository = teamHackathonRepository;
        this.membroTeamService = membroTeamService;
        this.membroTeamRepository = membroTeamRepository;
        this.hackathonService = hackathonService;
        this.accountService = accountService;
    }


    /**
     * Crea un nuovo team nel sistema.
     * Verifica che l'utente creatore non sia già membro di un altro team.
     *
     * @param nome Nome del team
     * @param descrizione Descrizione del team
     * @param idUtente ID dell'utente che crea il team
     * @return true se la creazione è riuscita, false altrimenti
     */
    public boolean creaTeam(String nome, String descrizione, Long idUtente) {
        // Validazione input base
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        if(accountService.findById(idUtente) == null) return false;

        // Verifica che l'utente non sia già membro di un team
        if (!membroTeamService.verificaDisponibilitaMembro(idUtente)) {
            return false; // Utente già membro di un team
        }

        // Crea e persisti il team
        Team nuovoTeam = new Team(nome, descrizione);
        repository.save(nuovoTeam);

        // Aggiungi il creatore come primo membro (amministratore)
        membroTeamService.addMembro(idUtente, nuovoTeam.getId());

        return true;
    }


    /**
     * Verifica la disponibilità di un utente a unirsi a un team.
     * Un utente è disponibile se non è già membro di un altro team.
     *
     * @param account Utente da verificare
     * @return true se l'utente è disponibile, false altrimenti
     */
    public boolean verificaDisponibilitaMembro(Account account) {
        if (account == null) {
            return false;
        }
        return membroTeamService.verificaDisponibilitaMembro(account.getId());
    }


    /**
     * Restituisce un team specifico tramite il suo ID.
     *
     * @param idTeam ID del team da recuperare
     * @return Il team trovato, o null se non esiste
     */
    public Team getTeamById(Long idTeam) {
        return repository.findById(idTeam).orElse(null);
    }


}
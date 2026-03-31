package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.TeamHackathonRepository;
import it.unicam.cs.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final TeamHackathonRepository teamHackathonRepository;
    private final MembroTeamService membroTeamService;
    private final UtenteService utenteService;

    public TeamService(TeamRepository repository,
                       TeamHackathonRepository teamHackathonRepository,
                       MembroTeamService membroTeamService,
                       UtenteService utenteService) {
        this.repository = repository;
        this.teamHackathonRepository = teamHackathonRepository;
        this.membroTeamService = membroTeamService;
        this.utenteService = utenteService;
    }


    /**
     * Crea un nuovo team nel sistema.
     * Verifica che l'utente creatore non sia già membro di un altro team.
     *
     * @param nome Nome del team
     * @param descrizione Descrizione del team
     * @param idUtenteCreatore ID dell'utente che crea il team
     * @return true se la creazione è riuscita, false altrimenti
     */
    public boolean creaTeam(String nome, String descrizione, Long idUtenteCreatore) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        if(utenteService.findById(idUtenteCreatore) == null) return false;

        if (!membroTeamService.verificaDisponibilitaMembro(idUtenteCreatore)) {
            return false; // Utente già membro di un team
        }

        Team nuovoTeam = new Team(nome, descrizione, idUtenteCreatore);
        repository.save(nuovoTeam);

        membroTeamService.addMembro(idUtenteCreatore, nuovoTeam.getId());

        return true;
    }

    /**
     * Verifica la disponibilità di un utente a unirsi a un team.
     * Un utente è disponibile se non è già membro di un altro team.
     *
     * @param utente Utente da verificare
     * @return true se l'utente è disponibile, false altrimenti
     */
    public boolean verificaDisponibilitaMembro(Utente utente) {
        if (utente == null) {
            return false;
        }
        return membroTeamService.verificaDisponibilitaMembro(utente.getId());
    }

    /**
     * Verifica la presenza di inviti duplicati per un utente.
     *
     * @param utente Utente da verificare
     * @return true se esistono inviti duplicati, false altrimenti
     */
    public boolean checkDuplicateInviti(Utente utente) {
        // TODO: Implementare con InvitoController quando disponibile
        return false;
    }


    /**
     * Restituisce tutti i team presenti nel sistema.
     *
     * @return Lista di tutti i team
     */
    public List<Team> getListaTeam() {
        return repository.findAll();
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

    /**
     * Restituisce tutti i team iscritti a un hackathon specifico.
     * @param hackathon hackathon di cui recuperare i team
     * @return lista di team iscritti a hackathon
     */
    public List<Team> getTeam(Hackathon hackathon) {
        if (hackathon == null) throw new NullPointerException("Hackathon non valido");
        return teamHackathonRepository.findAll().stream()
                .filter(th -> th.getHackathon().equals(hackathon))
                .map(TeamHackathon::getTeam)
                .toList();
    }
}
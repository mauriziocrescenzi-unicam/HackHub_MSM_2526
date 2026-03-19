package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;
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
public class TeamService {

    private static TeamService service;
    private final StandardPersistence<Team> teamPersistence;
    private final StandardPersistence<TeamHackathon> teamHackathonPersistence;
    private final StandardPersistence<Hackathon> hackathonPersistence;
    private final MembroTeamService membroTeamService;
    private final HackathonService hackathonService;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza i layer di persistenza per le entità gestite.
     */
    private TeamService() {
        this.teamPersistence = new StandardPersistence<>(Team.class);
        this.teamHackathonPersistence = new StandardPersistence<>(TeamHackathon.class);
        this.membroTeamService = MembroTeamService.getInstance();
        this.hackathonService = HackathonService.getInstance();
        this.hackathonPersistence = new StandardPersistence<>(Hackathon.class);
    }

    /**
     * Restituisce l'istanza Singleton del TeamController.
     * Crea una nuova istanza se non esiste ancora.
     *
     * @return Istanza Singleton di TeamController
     */
    public static TeamService getInstance() {
        if (service == null) {
            service = new TeamService();
        }
        return service;
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
        // Validazione input base
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }

        // Verifica che l'utente non sia già membro di un team
        if (!membroTeamService.verificaDisponibilitaMembro(idUtenteCreatore)) {
            return false; // Utente già membro di un team
        }

        // Crea e persisti il team
        Team nuovoTeam = new Team(nome, descrizione, idUtenteCreatore);
        teamPersistence.create(nuovoTeam);

        // Aggiungi il creatore come primo membro (amministratore)
        membroTeamService.addMembro(idUtenteCreatore, nuovoTeam.getId());

        return true;
    }

    /**
     * Verifica se un team è iscritto a un hackathon.
     *
     * @param team Team da verificare
     * @return true se il team è iscritto, false altrimenti
     */
    public boolean isIscrittoHackathon(Team team) {
        if (team == null || team.getHackathonIscritti() == null) {
            return false;
        }
        return !team.getHackathonIscritti().isEmpty();
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
     * Iscrive un team a un hackathon.
     * Verifica i requisiti di scadenza, dimensione del team e iscrizione precedente.
     *
     * @param hackathon Hackathon a cui iscriversi
     * @param team Team da iscrivere
     * @return true se l'iscrizione è riuscita, false altrimenti
     */
    public boolean iscrivereTeam(Hackathon hackathon, Team team) {
        if (team == null || hackathon == null) {
            return false;
        }

        // Verifica che il team non sia già iscritto
        if (isIscrittoHackathon(team)) {
            return false;
        }

        // Verifica scadenza iscrizioni
        if (hackathon.getScadenzaIscrizione().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Verifica requisiti dimensione team
        int maxMembri = hackathon.getDimensioneMassimoTeam();
        if (membroTeamService.getMembri(team.getId()).size() > maxMembri) {
            return false;
        }

        // Verifica requisiti minimi (almeno 1 membro)
        if (membroTeamService.getMembri(team.getId()).isEmpty()) {
            return false;
        }

        // Crea e persisti l'associazione TeamHackathon
        TeamHackathon teamHackathon = new TeamHackathon(team, hackathon);
        teamHackathonPersistence.create(teamHackathon);

        return true;
    }

    /**
     * Restituisce tutti gli hackathon a cui un team è iscritto.
     *
     * @param team Team di cui recuperare le iscrizioni
     * @return Lista di hackathon a cui il team è iscritto
     */
    public List<Hackathon> getHackathon(Team team) {
        if (team == null || team.getHackathonIscritti() == null) {
            return new ArrayList<>();
        }

        // Estrae gli hackathon dalle associazioni TeamHackathon
        return team.getHackathonIscritti().stream()
                .filter(TeamHackathon::isIscritto)
                .map(TeamHackathon::getHackathon)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutti i team presenti nel sistema.
     *
     * @return Lista di tutti i team
     */
    public List<Team> getListaTeam() {
        return teamPersistence.getAll();
    }

    /**
     * Restituisce un team specifico tramite il suo ID.
     *
     * @param idTeam ID del team da recuperare
     * @return Il team trovato, o null se non esiste
     */
    public Team getTeamById(Long idTeam) {
        return teamPersistence.findById(idTeam);
    }

    /**
     * Restituisce tutti i team iscritti a un hackathon specifico.
     * @param hackathon hackathon di cui recuperare i team
     * @return lista di team iscritti a hackathon
     */
    public List<Team> getTeam(Hackathon hackathon) {
        if (hackathon == null) throw new NullPointerException("Hackathon non valido");
        return teamHackathonPersistence.getAll().stream()
                .filter(th -> th.getHackathon().equals(hackathon))
                .map(TeamHackathon::getTeam)
                .toList();
    }

    public boolean checkIscrizioneHackathon(Long idTeam, long idHackathon) {
        if(idHackathon <0) throw new NullPointerException("Hackathon non valido");
        if(idTeam <0) throw new NullPointerException("Team non valido");
        return teamHackathonPersistence.getAll().stream()
                .anyMatch(th -> th.getTeam().getId().equals(idTeam)
                        && th.getHackathon().getId() ==idHackathon);

    }
    public boolean rimuoviTeam(long idTeam, long idHackathon){
        if(idTeam < 0) throw new IllegalArgumentException("Team non valido");
        if(idHackathon < 0) throw new IllegalArgumentException("Hackathon non valido");

        TeamHackathon teamHackathon = teamHackathonPersistence.getAll().stream()
                .filter(th -> th.getTeam().getId().equals(idTeam)
                        && th.getHackathon().getId() == idHackathon)
                .findFirst()
                .orElse(null);

        if (teamHackathon == null) {
            return false; // Iscrizione non trovata
        }

        teamHackathonPersistence.delete(teamHackathon);
        return true;
    }
}
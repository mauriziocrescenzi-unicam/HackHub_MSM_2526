package it.unicam.cs.Controller;

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
public class TeamController {

    private static TeamController controller;
    private final StandardPersistence<Team> teamPersistence;
    private final StandardPersistence<TeamHackathon> teamHackathonPersistence;
    private final MembroTeamController membroTeamController;
    private final HackathonController hackathonController;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza i layer di persistenza per le entità gestite.
     */
    private TeamController() {
        this.teamPersistence = new StandardPersistence<>(Team.class);
        this.teamHackathonPersistence = new StandardPersistence<>(TeamHackathon.class);
        this.membroTeamController = MembroTeamController.getInstance();
        this.hackathonController = HackathonController.getInstance();
    }

    /**
     * Restituisce l'istanza Singleton del TeamController.
     * Crea una nuova istanza se non esiste ancora.
     *
     * @return Istanza Singleton di TeamController
     */
    public static TeamController getInstance() {
        if (controller == null) {
            controller = new TeamController();
        }
        return controller;
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
        if (!membroTeamController.verificaDisponibilitaMembro(idUtenteCreatore)) {
            return false; // Utente già membro di un team
        }

        // Crea e persisti il team
        Team nuovoTeam = new Team(nome, descrizione, idUtenteCreatore);
        teamPersistence.save(nuovoTeam);

        // Aggiungi il creatore come primo membro (amministratore)
        membroTeamController.addMembro(idUtenteCreatore, nuovoTeam.getId());

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
        return membroTeamController.verificaDisponibilitaMembro(utente.getId());
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
        if (hackathon.getDataScadenzaIscrizioni().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Verifica requisiti dimensione team
        int maxMembri = hackathon.getDimensioneMassimaTeam();
        if (membroTeamController.getMembri(team).size() > maxMembri) {
            return false;
        }

        // Verifica requisiti minimi (almeno 1 membro)
        if (membroTeamController.getMembri(team).size() < 1) {
            return false;
        }

        // Crea e persisti l'associazione TeamHackathon
        TeamHackathon teamHackathon = new TeamHackathon(team, hackathon);
        teamHackathonPersistence.save(teamHackathon);

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
        return teamPersistence.getById(idTeam);
    }

    /**
     * Restituisce il controller per la gestione dei membri del team.
     *
     * @return MembroTeamController istanziato
     */
    public MembroTeamController getMembroTeamController() {
        return membroTeamController;
    }
}
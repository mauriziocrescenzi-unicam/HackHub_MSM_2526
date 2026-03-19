package it.unicam.cs.Service;

import it.unicam.cs.model.MembroTeam;
import it.unicam.cs.model.Team;
import it.unicam.cs.model.Utente;
import it.unicam.cs.persistence.StandardPersistence;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsabile della gestione dei membri dei team nel sistema HackHub.
 * Implementa il pattern Singleton per garantire un'unica istanza del controller.
 * Contiene tutta la logica di business per operazioni sui membri dei team.
 *
 */
public class MembroTeamService {

    private static MembroTeamService service;
    private final StandardPersistence<MembroTeam> membroTeamPersistence;
    private final StandardPersistence<Utente> utentePersistence;
    private final StandardPersistence<Team> teamPersistence;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza i layer di persistenza per le entità gestite.
     */
    private MembroTeamService() {
        this.membroTeamPersistence = new StandardPersistence<>(MembroTeam.class);
        this.utentePersistence = new StandardPersistence<>(Utente.class);
        this.teamPersistence = new StandardPersistence<>(Team.class);
    }

    /**
     * Restituisce l'istanza Singleton del MembroTeamController.
     * Crea una nuova istanza se non esiste ancora.
     *
     * @return Istanza Singleton di MembroTeamController
     */
    public static MembroTeamService getInstance() {
        if (service == null) {
            service = new MembroTeamService();
        }
        return service;
    }

    /**
     * Verifica se un utente è già membro di un team.
     * Un utente può appartenere a un solo team alla volta.
     *
     * @param utente Utente da verificare
     * @return true se l'utente è già membro di un team, false altrimenti
     */
    public boolean isMembroTeam(Utente utente) {
        if (utente == null) {
            return false;
        }
        return isMembroTeam(utente.getId());
    }

    /**
     * Verifica se un utente è già membro di un team dato il suo ID.
     *
     * @param idUtente ID dell'utente da verificare
     * @return true se l'utente è già membro di un team, false altrimenti
     */
    public boolean isMembroTeam(Long idUtente) {
        if (idUtente == null) {
            return false;
        }

        // Cerca nel database se esiste un MembroTeam per questo utente
        List<MembroTeam> tuttiMembri = membroTeamPersistence.getAll();
        for (MembroTeam m : tuttiMembri) {
            if (m.getUtente().getId().equals(idUtente)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica la disponibilità di un utente a unirsi a un team.
     * Un utente è disponibile se NON è già membro di un team.
     *
     * @param idUtente ID dell'utente da verificare
     * @return true se l'utente è disponibile, false altrimenti
     */
    public boolean verificaDisponibilitaMembro(Long idUtente) {
        return !isMembroTeam(idUtente);
    }

    /**
     * Restituisce il MembroTeam dell'utente
     */
    public MembroTeam getMembro(Utente utente) {//TODO cambio get all
        for (MembroTeam membro : membroTeamPersistence.getAll()) {
            if (membro.getId().getUtenteId().equals(utente.getId()))
                return membro;
        }
        return null;
    }

    /**
     * Restituisce la lista dei membri di un team specifico.
     *
     * @param idTeam Team di cui recuperare i membri
     * @return Lista di MembroTeam del team specificato
     */
    public List<MembroTeam> getMembri(long idTeam) {
        if (idTeam < 0) {
            return new ArrayList<>();
        }

        // Filtra i membri per il team specificato
        List<MembroTeam> tuttiMembri = membroTeamPersistence.getAll();
        return tuttiMembri.stream()
                .filter(m -> m.getTeam().getId().equals(idTeam))
                .collect(Collectors.toList());
    }

    /**
     * Aggiunge un utente come membro di un team.
     * Verifica che l'utente non sia già membro di un altro team.
     *
     * @param idUtente ID dell'utente da aggiungere al team
     * @param idTeam ID del team a cui aggiungere l'utente
     * @return true se l'aggiunta è andata a buon fine, false altrimenti
     */
    public boolean addMembro(Long idUtente, Long idTeam) {
        // Validazione input
        if (idUtente == null || idTeam == null) {
            return false;
        }

        // Verifica che l'utente non sia già membro di un team
        if (isMembroTeam(idUtente)) {
            return false; // Utente già membro di un team
        }

        // Recupera le entità dal database
        Utente utente = utentePersistence.findById(idUtente);
        Team team = teamPersistence.findById(idTeam);

        if (utente == null || team == null) {
            return false;
        }

        // Crea la relazione MembroTeam
        MembroTeam nuovoMembro = new MembroTeam(utente, team);

        // Persisti la relazione
        membroTeamPersistence.create(nuovoMembro);

        return true;
    }

    /**
     * Rimuove un membro da un team.
     *
     * @param membro MembroTeam da rimuovere
     * @return true se la rimozione è andata a buon fine, false altrimenti
     */
    public boolean rimuoviMembro(MembroTeam membro) {
        if (membro == null) {
            return false;
        }

        membroTeamPersistence.delete(membro);
        return true;
    }

    /**
     * Restituisce tutti i membri presenti nel sistema.
     *
     * @return Lista di tutti i membri
     */
    public List<MembroTeam> getAllMembri() {
        return membroTeamPersistence.getAll();
    }

    /**
     * Restituisce il numero totale di membri nel sistema.
     *
     * @return Numero totale di membri
     */
    public int countMembriTotali() {
        return getAllMembri().size();
    }
}
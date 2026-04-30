package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service responsabile della gestione dei membri dei team nel sistema HackHub.
 * Contiene tutta la logica di business per le operazioni sui membri, incluse
 * aggiunta, rimozione, abbandono e verifica della disponibilità.
 */
@Service
@Transactional
public class MembroTeamService {

    private final MembroTeamRepository repository;
    private final AccountRepository accountRepository;
    private final TeamRepository teamRepository;
    private final TeamHackathonRepository teamHackathonRepository;
    /**
     * Costruisce un'istanza di {@code MembroTeamService} con le dipendenze necessarie.
     *
     * @param repository               repository per l'accesso ai membri del team
     * @param accountRepository        repository per l'accesso agli account
     * @param teamRepository           repository per l'accesso ai team
     * @param teamHackathonRepository  repository per l'accesso alle associazioni team-hackathon
     */
    public MembroTeamService(MembroTeamRepository repository,
                             AccountRepository accountRepository,
                             TeamRepository teamRepository,
                             TeamHackathonRepository teamHackathonRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.teamRepository = teamRepository;
        this.teamHackathonRepository = teamHackathonRepository;
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
        List<MembroTeam> tuttiMembri = repository.findAll();
        for (MembroTeam m : tuttiMembri) {
            if (m.getAccount().getId().equals(idUtente)) {
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
     * Restituisce il {@link MembroTeam} associato all'account specificato.
     *
     * @param account l'account di cui recuperare il membro team
     * @return il {@link MembroTeam} corrispondente, oppure {@code null} se non trovato
     */
    public MembroTeam getMembro(Account account) {
        for (MembroTeam membro : repository.findAll()) {
            if (membro.getId().getUtenteId().equals(account.getId()))
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
        List<MembroTeam> tuttiMembri = repository.findAll();
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
        Account utente = accountRepository.getReferenceById(idUtente);
        Team team = teamRepository.getReferenceById(idTeam);

        // Crea la relazione MembroTeam
        MembroTeam nuovoMembro = new MembroTeam(utente, team);

        // Persisti la relazione
        repository.save(nuovoMembro);

        return true;
    }


    /**
     * Elimina completamente un team dal sistema.
     * Questo metodo dovrebbe essere chiamato solo quando un team rimane senza membri.
     *
     * @param idTeam ID del team da eliminare
     * @return true se l'eliminazione è avvenuta con successo, false altrimenti
     */
    public boolean eliminaTeam(Long idTeam) {
        if (idTeam == null || idTeam < 0) {
            return false;
        }

        // Verifica che il team esista
        Team team = teamRepository.findById(idTeam).orElse(null);
        if (team == null) {
            return false; // Team non trovato
        }

        // Verifica che il team non abbia membri (usa il repository direttamente)
        List<MembroTeam> membri = repository.findByTeamId(idTeam);
        if (!membri.isEmpty()) {
            return false; // Il team ha ancora membri
        }

        // Rimuovi il team da tutti gli hackathon associati
        List<TeamHackathon> associazioni = teamHackathonRepository.findByTeamId(idTeam);
        teamHackathonRepository.deleteAll(associazioni);

        // Elimina il team
        teamRepository.deleteById(idTeam);
        return true;
    }

    /**
     * Gestisce l'abbandono volontario di un membro da un team.
     * Se dopo l'abbandono il team rimane senza membri, il team viene eliminato.
     *
     * @param idMembro ID del membro che abbandona
     * @param idTeam ID del team da abbandonare
     * @return true se l'abbandono è avvenuto con successo, false altrimenti
     */
    public boolean abbandonaTeam(Long idMembro, Long idTeam) {
        if (idMembro == null || idTeam == null || idMembro <= 0 || idTeam <= 0) {
            return false;
        }
        MembroTeam membroTeam = repository.findByAccountIdAndTeamId(idMembro, idTeam)
                .orElse(null);
        if (membroTeam == null) {
            return false; // Membro non trovato nel team specificato
        }
        // Rimuovi l'associazione membro-team
        repository.delete(membroTeam);
        // Verifica se il team è rimasto senza membri
        List<MembroTeam> membriRimanenti = repository.findByTeamId(idTeam);
        if (membriRimanenti.isEmpty()) {
            eliminaTeam(idTeam);
        }
        return true;
    }

    /**
     * Elimina un membro specifico da un team.
     * Verifica che entrambi i membri (chi elimina e chi viene eliminato) appartengano allo stesso team.
     *
     * @param idMembroCheElimina ID del membro che sta effettuando l'eliminazione
     * @param idMembroDaEliminare ID del membro da eliminare
     * @param idTeam ID del team
     * @return true se l'eliminazione è avvenuta con successo, false altrimenti
     */
    public boolean eliminaMembro(Long idMembroCheElimina, Long idMembroDaEliminare, Long idTeam) {
        if (idMembroCheElimina == null || idMembroDaEliminare == null || idTeam == null) {
            return false;
        }
        if (idMembroCheElimina <= 0 || idMembroDaEliminare <= 0 || idTeam <= 0) {
            return false;
        }
        MembroTeam membroCheElimina = repository.findByAccountIdAndTeamId(idMembroCheElimina, idTeam)
                .orElse(null);
        if (membroCheElimina == null) {
            return false; // Il membro che elimina non appartiene al team
        }
        MembroTeam membroDaRimuovere = repository.findByAccountIdAndTeamId(idMembroDaEliminare, idTeam)
                .orElse(null);
        if (membroDaRimuovere == null) {
            return false; // Membro da eliminare non trovato nel team
        }
        repository.delete(membroDaRimuovere);
        List<MembroTeam> membriRimanenti = repository.findByTeamId(idTeam);
        if (membriRimanenti.isEmpty()) {
            eliminaTeam(idTeam);
        }
        return true;
    }

    /**
     * Recupera il MembroTeam associato a un account specifico.
     * @param idAccount ID dell'account
     * @return Il MembroTeam se trovato, null altrimenti
     */
    public MembroTeam getMembroById(Long idAccount) {
        if (idAccount == null) return null;
        return repository.findByAccountId(idAccount).orElse(null);
    }
}
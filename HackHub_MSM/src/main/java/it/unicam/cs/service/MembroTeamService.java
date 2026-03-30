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
 * Controller responsabile della gestione dei membri dei team nel sistema HackHub.
 * Implementa il pattern Singleton per garantire un'unica istanza del controller.
 * Contiene tutta la logica di business per operazioni sui membri dei team.
 *
 */
@Service
@Transactional
public class MembroTeamService {

    private final MembroTeamRepository repository;
    private final UtenteRepository utenteRepository;
    private final TeamRepository teamRepository;
    private final TeamHackathonRepository teamHackathonRepository;
    private final HackathonService hackathonService;
    private final RichiestaSupportoService richiestaSupportoService;

    public MembroTeamService(MembroTeamRepository repository,
                             UtenteRepository utenteRepository,
                             TeamRepository teamRepository,
                             TeamHackathonRepository teamHackathonRepository,
                             HackathonService hackathonService,
                             RichiestaSupportoService richiestaSupportoService) {
        this.repository = repository;
        this.utenteRepository = utenteRepository;
        this.teamRepository = teamRepository;
        this.teamHackathonRepository = teamHackathonRepository;
        this.hackathonService = hackathonService;
        this.richiestaSupportoService = richiestaSupportoService;
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
        List<MembroTeam> tuttiMembri = repository.findAll();
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
        for (MembroTeam membro : repository.findAll()) {
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
        Utente utente = utenteRepository.getReferenceById( idUtente);
        Team team = teamRepository.getReferenceById(idTeam);

        // Crea la relazione MembroTeam
        MembroTeam nuovoMembro = new MembroTeam(utente, team);

        // Persisti la relazione
        repository.save(nuovoMembro);

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

        repository.delete(membro);
        return true;
    }

    /**
     * Restituisce tutti i membri presenti nel sistema.
     *
     * @return Lista di tutti i membri
     */
    public List<MembroTeam> getAllMembri() {
        return repository.findAll();
    }

    /**
     * Restituisce il numero totale di membri nel sistema.
     *
     * @return Numero totale di membri
     */
    public int countMembriTotali() {
        return getAllMembri().size();
    }

    /**
     * Gestisce l'abbandono di un membro da un team.
     * Verifica che il membro esista nel team prima di procedere con la rimozione.
     * Questo metodo è progettato per essere chiamato quando un membro decide
     * volontariamente di lasciare il team.
     *
     * @param idMembro ID del membro che abbandona il team
     * @param idTeam ID del team da abbandonare
     * @return true se l'abbandono è avvenuto con successo, false se il membro
     *         non è stato trovato nel team o si è verificato un errore
     */
    public boolean abbandonaTeam(Long idMembro, Long idTeam) {
        if (idMembro == null || idTeam == null) {
            return false;
        }

        // Trova il membro nel team
        MembroTeam membroDaRimuovere = repository.findAll().stream()
                .filter(m -> m.getUtente().getId().equals(idMembro) &&
                        m.getTeam().getId().equals(idTeam))
                .findFirst()
                .orElse(null);

        if (membroDaRimuovere == null) {
            return false; // Membro non trovato nel team
        }

        return rimuoviMembro(membroDaRimuovere);
    }

    /**
     * Elimina un membro specifico da un team.
     * Metodo utilizzato per la rimozione forzata di un membro
     * Segue la stessa logica di abbandonaTeam ma con semantica diversa.
     *
     * @param idMembro ID del membro da eliminare
     * @param idTeam ID del team da cui rimuovere il membro
     * @return true se l'eliminazione è avvenuta con successo, false altrimenti
     */
    public boolean eliminaMembro(Long idMembro, Long idTeam) {
        // Riutilizza la logica di abbandonaTeam
        return abbandonaTeam(idMembro, idTeam);
    }

    public List<Hackathon> getListahackathon(StatoHackathon stato,Long idMembroTeam){
        if (stato == null) throw new IllegalArgumentException("Stato non valido.");
        if (idMembroTeam == null || idMembroTeam <= 0) throw new IllegalArgumentException("MembroTeam non valido.");

        MembroTeam membroTeam = repository.findById(idMembroTeam).orElse(null);
        if (membroTeam == null) throw new IllegalArgumentException("MembroTeam non trovato.");
        //prendo la lista di teamhackathon riguardate a il team del membroTeam
        List<TeamHackathon> teamHackathonList= teamHackathonRepository.findAll().stream().filter(th ->
                th.getHackathon().getStato() == stato && Objects.equals(th.getTeam().getId(), membroTeam.getTeam().getId())).toList();
        return teamHackathonList.stream().map(TeamHackathon::getHackathon).toList();
    }

    /**
     * Verifica se il team del membro è iscritto ad almeno un hackathon.
     * Corrisponde a isIscrittoHackathon(teamMittente) nel sequence diagram.
     *
     * @param idMembroTeam ID del membro del team
     * @return Lista degli hackathon a cui il team è iscritto, lista vuota se nessuno
     */
    public List<Hackathon> isIscrittoHackathon(Long idMembroTeam) {
        if (idMembroTeam == null || idMembroTeam <= 0) {
            return new ArrayList<>();
        }
        MembroTeam membroTeam = repository.findById(idMembroTeam).orElse(null);
        if (membroTeam == null) {
            return new ArrayList<>();
        }
        return teamHackathonRepository.findAll().stream()
                .filter(th -> Objects.equals(th.getTeam().getId(), membroTeam.getTeam().getId()))
                .map(TeamHackathon::getHackathon)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se almeno uno degli hackathon del team è nello stato richiesto.
     * Corrisponde a checkStato(listaHackathon, "in corso") nel sequence diagram.
     *
     * @param listaHackathon Lista di hackathon da verificare
     * @param stato          Stato richiesto
     * @return true se almeno un hackathon è nello stato richiesto, false altrimenti
     */
    public boolean checkStato(List<Hackathon> listaHackathon, StatoHackathon stato) {
        if (listaHackathon == null || listaHackathon.isEmpty() || stato == null) {
            return false;
        }
        for (Hackathon h : listaHackathon) {
            if (h.getStato() == stato) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invia una richiesta di supporto per un hackathon specifico.
     * Corrisponde a inviaRichiestaSupporto(teamRichiedente, descrizioneRichiesta, data)
     * nel sequence diagram. Delega la verifica e la creazione al RichiestaSupportoService.
     *
     * @param idMembroTeam         ID del membro del team che invia la richiesta
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     * @param dataInvio            Data e ora di invio
     * @param idHackathon          ID dell'hackathon di riferimento
     * @return true se la richiesta è stata inviata con successo, false altrimenti
     */
    public boolean inviaRichiestaSupporto(Long idMembroTeam,
                                          String descrizioneRichiesta,
                                          LocalDateTime dataInvio,
                                          Long idHackathon) {
        if (idMembroTeam == null || idHackathon == null) return false;

        MembroTeam membroTeam = repository.findById(idMembroTeam).orElse(null);
        if (membroTeam == null) return false;

        Long idTeam = membroTeam.getTeam().getId();

        Hackathon hackathon = null;
        List<TeamHackathon> lista = teamHackathonRepository.findAll();
        for (TeamHackathon th : lista) {
            if (Objects.equals(th.getTeam().getId(), idTeam) &&
                    Objects.equals(th.getHackathon().getId(), idHackathon)) {
                hackathon = th.getHackathon();
                break;
            }
        }
        if (hackathon == null) return false;

        RichiestaSupporto richiesta = richiestaSupportoService.inviaRichiestaSupporto(
                idTeam, descrizioneRichiesta, dataInvio, hackathon);

        return richiesta != null;
    }

}
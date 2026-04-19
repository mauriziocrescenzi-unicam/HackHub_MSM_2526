package it.unicam.cs.service;

import it.unicam.cs.dto.ClassificaTeamDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.repository.HackathonRepository;
import it.unicam.cs.repository.SottomissioneRepository;
import it.unicam.cs.service.facade.RepositoryFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsabile della gestione delle sottomissioni nel sistema HackHub.
 * Implementa il pattern Singleton.
 * Gestisce i casi d'uso: inviare, aggiornare, valutare e accedere alle
 * sottomissioni.
 */
@Service
@Transactional
public class SottomissioneService {

    private final SottomissioneRepository repository;
    private final HackathonService hackathonService;
    private final TeamService teamService;
    private final TeamHackathonService teamHackathonService;
    private final HackathonRepository hackathonRepository;
    private final RepositoryFacade repositoryFacade;
    private final MembroTeamService membroTeamService;


    public SottomissioneService(SottomissioneRepository repository, HackathonService hackathonService,
                                TeamService teamService, TeamHackathonService teamHackathonService, HackathonRepository hackathonRepository,
                                RepositoryFacade repositoryFacade, MembroTeamService membroTeamService) {
        this.repository = repository;
        this.hackathonService = hackathonService;
        this.teamService = teamService;
        this.teamHackathonService = teamHackathonService;
        this.hackathonRepository = hackathonRepository;
        this.repositoryFacade = repositoryFacade;
        this.membroTeamService = membroTeamService;
    }
    private boolean verificaSottomissione(String nome, String link, Team team, Long idHackathon){
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null)
            return false;
        // Team deve essere iscritto all'hackathon
        if (!teamHackathonService.checkIscrizioneHackathon(team.getId(), idHackathon))
            return false;
        // Hackathon deve essere IN_CORSO
        if (hackathon.getStato() != StatoHackathon.IN_CORSO)
            return false;
        // Non deve esistere già una sottomissione
        if (isPresente(team.getId(), idHackathon))
            return false;
        // La dataFine dell'hackathon non deve essere passata
        if (hackathon.getDataFine().isBefore(LocalDateTime.now()))
            return false;
        // Valida nome e link
        if (link == null || link.isBlank())
            return false;

        return repositoryFacade.validaLink(link);
    }

    /**
     * Invia una nuova sottomissione per un team in un hackathon.
     *
     * @param nome        Nome della sottomissione
     * @param link        Link al progetto
     * @param account      account del membro del team che invia la sottomissione
     * @param idHackathon ID dell'hackathon
     * @return true se l'invio è riuscito, false altrimenti
     */
    public boolean inviaSottomissione(String nome, String link, Account account, Long idHackathon) {
        Team team = membroTeamService.getMembro(account).getTeam();
        if (team == null) return false;
        //verifica se le informazioni vanno bene
        if (!verificaSottomissione(nome, link, team, idHackathon))
            return false;
        // Crea la sottomissione
        Sottomissione nuova = new Sottomissione(nome, link, team.getId(), idHackathon);
        repository.save(nuova);
        return true;
    }

    /**
     * Aggiorna la sottomissione esistente per un team in un hackathon.
     *
     * @param nome        Nuovo nome della sottomissione
     * @param link        Nuovo link al progetto
     * @param account      account del membro del team che invia la sottomissione
     * @param idHackathon ID dell'hackathon
     * @return true se l'aggiornamento è riuscito, false altrimenti
     */
    public boolean aggiornaSottomissione(String nome, String link, Account account, Long idHackathon) {
        Team team = membroTeamService.getMembro(account).getTeam();
        if (team == null) return false;
        //verifica se le informazioni vanno bene
        if (!verificaSottomissione(nome, link, team, idHackathon))
            return false;
        // Deve esistere già una sottomissione
        if (!isPresente(team.getId(), idHackathon))
            return false;
        // Aggiorna con setInfo — aggiorna anche dataInvio a now()
        Sottomissione esistente = getSottomissioneByTeamHackathon(team.getId(), idHackathon);
        if (esistente == null)
            return false;
        esistente.setInfo(nome, link);
        repository.save(esistente);
        return true;
    }

    /**
     * Valuta una sottomissione assegnando voto e giudizio.
     * 
     * @param sottomissione La sottomissione da valutare
     * @param voto          Punteggio numerico (0-10)
     * @param giudizio      Giudizio scritto
     * @return true se la valutazione è riuscita, false altrimenti
     */
    public boolean valutaSottomissione(Sottomissione sottomissione, int voto, String giudizio) {
        if (sottomissione == null)
            return false;
        // Non deve essere già stata valutata
        if (isSottomissioneValutata(sottomissione))
            return false;
        // Valida voto e giudizio
        if (!checkValutazione(voto, giudizio))
            return false;
        // Imposta la valutazione
        sottomissione.setValutazione(voto, giudizio);
        repository.save(sottomissione);
        return true;
    }

    /**
     * Restituisce la lista delle sottomissioni per un dato hackathon.
     * Usato sia dal Membro Staff (accedere sottomissioni team)
     * che dal Giudice (valutare sottomissioni).
     * Corrisponde a getSottomissioni(hackathon) del sequence diagram.
     *
     * @param hackathon L'hackathon di cui recuperare le sottomissioni
     * @return Lista di sottomissioni dell'hackathon
     */
    public List<Sottomissione> getSottomissioni(Hackathon hackathon) {
        if (hackathon == null)
            throw new IllegalArgumentException("Hackathon non valido.");
        List<Sottomissione> tutte = repository.findAll();
        List<Sottomissione> risultato = new ArrayList<>();
        for (Sottomissione s : tutte) {
            if (s.getIdHackathon().equals(hackathon.getId())) {
                risultato.add(s);
            }
        }
        return risultato;
    }

    /**
     * Verifica se esiste già una sottomissione per il team nell'hackathon.
     * Corrisponde a isPresente(idTeam, idHackathon) del sequence diagram.
     *
     * @param idTeam      ID del team
     * @param idHackathon ID dell'hackathon
     * @return true se esiste già una sottomissione, false altrimenti
     */
    public boolean isPresente(Long idTeam, Long idHackathon) {
        List<Sottomissione> tutte = repository.findAll();
        for (Sottomissione s : tutte) {
            if (s.getIdTeam().equals(idTeam) && s.getIdHackathon().equals(idHackathon)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se una sottomissione è già stata valutata.
     * Voto iniziale è -1, quindi voto >= 0 significa valutata.
     * Corrisponde a isSottomissioneValutata(sottomissione) del sequence diagram.
     *
     * @param sottomissione La sottomissione da verificare
     * @return true se già valutata, false altrimenti
     */
    public boolean isSottomissioneValutata(Sottomissione sottomissione) {
        if (sottomissione == null)
            throw new IllegalArgumentException("Sottomissione non valida.");
        return sottomissione.getVoto() >= 0 && sottomissione.getGiudizio() != null;
    }


    /**
     * Valida voto e giudizio prima della valutazione.
     * Corrisponde a checkValutazione(voto, giudizio) del sequence diagram.
     * La validazione di dettaglio è anche nel core (setValutazione).
     *
     * @param voto     Voto da validare
     * @param giudizio Giudizio da validare
     * @return true se validi, false altrimenti
     */
    public boolean checkValutazione(int voto, String giudizio) {
        if (voto < 0 || voto > 10)
            return false;
        if (giudizio == null || giudizio.isBlank())
            return false;
        return true;
    }

    /**
     * Recupera la sottomissione di un team per un hackathon specifico.
     *
     * @param idTeam      ID del team
     * @param idHackathon ID dell'hackathon
     * @return La sottomissione trovata, o null se non esiste
     */
    public Sottomissione getSottomissioneByTeamHackathon(Long idTeam, Long idHackathon) {
        List<Sottomissione> tutte = repository.findAll();
        for (Sottomissione s : tutte) {
            if (s.getIdTeam().equals(idTeam) && s.getIdHackathon().equals(idHackathon)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Recupera una sottomissione specifica per ID.
     *
     * @param id ID della sottomissione da recuperare
     * @return La sottomissione corrispondente all'ID
     */
    public Sottomissione getSottomissioneById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Id non valido.");
        return repository.findById(id).orElse(null);
    }

    /**
     * Calcola la classifica dei team per un determinato hackathon.
     * La classifica è ordinata per punteggio decrescente (dal primo all'ultimo
     * posto).
     * <p>
     * Questo metodo recupera tutte le sottomissioni valutate per l'hackathon
     * e le ordina in base al voto ricevuto.
     * </p>
     *
     * @param hackathon Hackathon per cui calcolare la classifica
     * @return Lista di {@link ClassificaTeamDTO} ordinata per punteggio
     *         decrescente,
     *         o lista vuota se non ci sono sottomissioni valutate
     */
    public List<ClassificaTeamDTO> getClassifica(Hackathon hackathon) {
        if (hackathon == null) {
            return new ArrayList<>();
        }
        // Recupera tutte le sottomissioni dell'hackathon
        List<Sottomissione> sottomissioni = getSottomissioni(hackathon);

        List<ClassificaTeamDTO> classifica = new ArrayList<>();

        for (Sottomissione s : sottomissioni) {
            // Include solo sottomissioni valutate
            if (isSottomissioneValutata(s)) {
                Team team = teamService.getTeamById(s.getIdTeam());
                if (team != null) {
                    classifica.add(new ClassificaTeamDTO(
                            team,
                            s.getVoto(),
                            s.getGiudizio()));
                }
            }
        }

        // Ordina per punteggio decrescente
        classifica.sort((c1, c2) -> Double.compare(c2.getPunteggio(), c1.getPunteggio()));

        // Assegna le posizioni
        for (int i = 0; i < classifica.size(); i++) {
            classifica.get(i).setPosizione(i + 1);
        }

        return classifica;
    }

    /**
     * Proclama il team vincitore di un hackathon.
     * <p>
     * Verifica che:
     * - L'hackathon esista
     * - L'hackathon sia in stato IN_VALUTAZIONE
     * - Tutte le sottomissioni siano state valutate
     * - Il team proclamato sia effettivamente primo in classifica
     * </p>
     *
     * @param hackathon     Hackathon per cui proclamare il vincitore
     * @param teamVincitore Team da proclamare vincitore
     * @return true se la proclamazione è riuscita, false altrimenti
     */
    public boolean proclamaTeamVincitore(Hackathon hackathon, Team teamVincitore) {
        if (hackathon == null || teamVincitore == null) {
            return false;
        }
        if (!hackathonService.checkStato(List.of(hackathon), StatoHackathon.IN_VALUTAZIONE)) {
            return false;
        }

        // Verifica che tutte le sottomissioni siano state valutate
        List<Sottomissione> sottomissioni = getSottomissioni(hackathon);

        for (Sottomissione s : sottomissioni) {
            if (!isSottomissioneValutata(s)) {
                return false; // Non tutte le sottomissioni sono valutate
            }
        }

        hackathon.setVincitore(teamVincitore);
        hackathon.setStato(StatoHackathon.CONCLUSO);
        hackathonRepository.save(hackathon);

        return true;
    }

    /**
     * Restituisce la lista degli hackathon assegnati a un giudice filtrati per stato.
     * @param idAccount ID dell'account che fa da giudice
     * @param stato Stato dell'hackathon da filtrare
     * @return Lista di Hackathon
     */
    public List<Hackathon> getListaHackathonPerGiudice(Long idAccount, StatoHackathon stato) {
        if (stato == null) throw new IllegalArgumentException("Stato non valido.");
        if (idAccount == null || idAccount <= 0) throw new IllegalArgumentException("Account non valido.");
        // Filtra gli hackathon dove il giudice assegnato corrisponde all'account e lo stato corrisponde
        return hackathonRepository.findAll().stream()
                .filter(h -> h.getGiudice() != null)
                .filter(h -> h.getGiudice().getId().equals(idAccount))
                .filter(h -> h.getStato() == stato)
                .toList();
    }
}
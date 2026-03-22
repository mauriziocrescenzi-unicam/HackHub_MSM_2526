package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsabile della gestione delle sottomissioni nel sistema HackHub.
 * Implementa il pattern Singleton.
 * Gestisce i casi d'uso: inviare, aggiornare, valutare e accedere alle sottomissioni.
 */
public class SottomissioneService {

    private static SottomissioneService instance;
    private final StandardPersistence<Sottomissione> persistence;
    private final HackathonService hackathonService;
    private final TeamService teamService;

    private SottomissioneService() {
        this.persistence = new StandardPersistence<>(Sottomissione.class);
        this.hackathonService = HackathonService.getInstance();
        this.teamService = TeamService.getInstance();
    }

    public static SottomissioneService getInstance() {
        if (instance == null)
            instance = new SottomissioneService();
        return instance;
    }

    /**
     * Invia una nuova sottomissione per un team in un hackathon.
     *
     * @param nome        Nome della sottomissione
     * @param link        Link al progetto
     * @param idTeam      ID del team mittente
     * @param idHackathon ID dell'hackathon
     * @return true se l'invio è riuscito, false altrimenti
     */
    public boolean inviaSottomissione(String nome, String link, Long idTeam, Long idHackathon) {
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return false;

        //  Team deve essere iscritto all'hackathon
        if (!teamService.checkIscrizioneHackathon(idTeam, idHackathon)) return false;
        //  Hackathon deve essere IN_CORSO
        if (hackathon.getStato() != StatoHackathon.IN_CORSO) return false;
        //  Non deve esistere già una sottomissione
        if (isPresente(idTeam, idHackathon)) return false;
        //  La dataFine dell'hackathon non deve essere passata
        if (hackathon.getDataFine().isBefore(LocalDateTime.now())) return false;
        //  Valida nome e link
        if (!verificaSottomissione(nome, link)) return false;
        //  Crea la sottomissione
        Sottomissione nuova = new Sottomissione(nome, link, idTeam, idHackathon);
        persistence.create(nuova);
        return true;
    }


    /**
     * Aggiorna la sottomissione esistente per un team in un hackathon.
     *
     * @param nome        Nuovo nome della sottomissione
     * @param link        Nuovo link al progetto
     * @param idTeam      ID del team
     * @param idHackathon ID dell'hackathon
     * @return true se l'aggiornamento è riuscito, false altrimenti
     */
    public boolean aggiornaSottomissione(String nome, String link, Long idTeam, Long idHackathon) {
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return false;
        //  Team deve essere iscritto all'hackathon
        if (!teamService.checkIscrizioneHackathon(idTeam, idHackathon)) return false;
        //  Hackathon deve essere IN_CORSO
        if (hackathon.getStato() != StatoHackathon.IN_CORSO) return false;
        //  Deve esistere già una sottomissione
        if (!isPresente(idTeam, idHackathon)) return false;
        //  La dataFine dell'hackathon non deve essere passata
        if (hackathon.getDataFine().isBefore(LocalDateTime.now())) return false;
        //  Valida nome e link
        if (!verificaSottomissione(nome, link)) return false;
        //  Aggiorna con setInfo — aggiorna anche dataInvio a now()
        Sottomissione esistente = getSottomissioneByTeamHackathon(idTeam, idHackathon);
        if (esistente == null) return false;
        esistente.setInfo(nome, link);
        persistence.update(esistente);
        return true;
    }

    /**
     * Valuta una sottomissione assegnando voto e giudizio.
     * @param sottomissione La sottomissione da valutare
     * @param voto          Punteggio numerico (0-10)
     * @param giudizio      Giudizio scritto
     * @return true se la valutazione è riuscita, false altrimenti
     */
    public boolean valutaSottomissione(Sottomissione sottomissione, int voto, String giudizio) {
        if (sottomissione == null) return false;
        //  Non deve essere già stata valutata
        if (isSottomissioneValutata(sottomissione)) return false;
        //  Valida voto e giudizio
        if (!checkValutazione(voto, giudizio)) return false;
        //  Imposta la valutazione
        sottomissione.setValutazione(voto, giudizio);
        persistence.update(sottomissione);
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
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non valido.");
        List<Sottomissione> tutte = persistence.getAll();
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
        List<Sottomissione> tutte = persistence.getAll();
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
        if (sottomissione == null) throw new IllegalArgumentException("Sottomissione non valida.");
        return sottomissione.getVoto() >= 0 && sottomissione.getGiudizio() != null;
    }

    /**
     * Valida nome e link della sottomissione.
     * Corrisponde a verificaSottomissione(nome, link) del sequence diagram.
     *
     * @param nome Nome da validare
     * @param link Link da validare
     * @return true se entrambi validi, false altrimenti
     */
    public boolean verificaSottomissione(String nome, String link) {
        if (nome == null || nome.isBlank()) return false;
        if (link == null || link.isBlank()) return false;
        return true;
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
        if (voto < 0 || voto > 10) return false;
        if (giudizio == null || giudizio.isBlank()) return false;
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
        List<Sottomissione> tutte = persistence.getAll();
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
        if (id == null) throw new IllegalArgumentException("Id non valido.");
        return persistence.findById(id);
    }
}
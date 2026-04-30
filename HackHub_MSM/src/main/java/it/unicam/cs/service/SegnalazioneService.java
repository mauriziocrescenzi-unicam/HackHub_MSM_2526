package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.SegnalazioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Service per la gestione delle segnalazioni dei team nel sistema HackHub.
 * Fornisce operazioni per creare, accettare, rifiutare e recuperare segnalazioni
 * effettuate dai mentori nei confronti dei team durante gli hackathon.
 */
@Service
@Transactional
public class SegnalazioneService {
    private final SegnalazioneRepository repository;
    private final TeamService teamService;
    private final HackathonService hackathonService;
    private final TeamHackathonService teamHackathonService;
    private final MembroDelloStaffService membroDelloStaffService;

    /**
     * Costruisce un'istanza di {@code SegnalazioneService} con le dipendenze necessarie.
     *
     * @param repository           repository per l'accesso alle segnalazioni
     * @param teamService          service per la gestione dei team
     * @param hackathonService     service per la gestione degli hackathon
     * @param teamHackathonService service per la gestione delle iscrizioni team-hackathon
     */
    public SegnalazioneService(SegnalazioneRepository repository, TeamService teamService, HackathonService hackathonService, TeamHackathonService teamHackathonService, MembroDelloStaffService membroDelloStaffService) {
        this.repository = repository;
        this.teamService = teamService;
        this.hackathonService = hackathonService;this.teamHackathonService = teamHackathonService;
        this.membroDelloStaffService = membroDelloStaffService;
    }


    /**
     * Verifica se la segnalazione è già stata effettuata
     * @param team team da segnalare
     * @param hackathon hackathon relativo alla segnalazione
     * @param mentore mentore che ha segnalato
     * @param motivazione motivazione della segnalazione
     * @return true se la segnalazione va bene
     */
    public boolean verificaSegnalazione(Team team,Hackathon hackathon, Account mentore, String motivazione){
        if (team == null) throw new IllegalArgumentException("Team non valido");
        if (motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non valido");
        if (mentore == null) throw new IllegalArgumentException("Mentore non valido");
        if(!hackathon.getMentori().contains(mentore))
            throw new IllegalArgumentException("Il tuo ruolo non consente di effettuare questa operazione");
        //controllo che non ci sia già una segnalazione
        return repository.findAll().stream()
                .anyMatch(s -> s.getTeam().equals(team)
                        && s.getHackathon().equals(hackathon)
                        && s.getMentore().equals(mentore));
    }

    /**
     * Segnala un team
     * @param idTeam id del team da segnalare
     * @param idHackathon id dell'hackathon relativo alla segnalazione
     * @param idMentore id del mentore che ha segnalato
     * @param motivazione motivazione della segnalazione
     * @return true se la segnalazione è andata a buon fine, false altrimenti
     */
    public boolean segnalaTeam(long idTeam,long idHackathon, long idMentore, String motivazione){
        if (idTeam < 0) throw new IllegalArgumentException("Team non valido");
        if (motivazione.isEmpty()||motivazione.isBlank()) throw new IllegalArgumentException("Motivazione non valida");
        if(idHackathon < 0) throw new IllegalArgumentException("Id hackathon non valido");
        if(idMentore < 0) throw new IllegalArgumentException("Id mentore non valido");

        Team team=teamService.getTeamById(idTeam);
        // Recupera hackathon e mentore dalle rispettive persistence
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        Account mentore = membroDelloStaffService.getMembroStaffById(idMentore);
        if (team == null ||hackathon==null|| mentore == null) {
            return false;
        }
        if (verificaSegnalazione(team,hackathon,mentore,motivazione)) return false;
        Segnalazione segnalazione = new Segnalazione(StatoSegnalazione.DA_GESTIRE,LocalDateTime.now(),motivazione,
                team, mentore, hackathon);
        repository.save(segnalazione);
        return true;

    }

    /**
     * Accetta una segnalazione
     * @param segnalazione segnalazione da accettare
     * @return true se l'accettazione
     */
    public boolean accettaSegnalazione(Segnalazione segnalazione){
        if (segnalazione==null) throw new NullPointerException("Segnalazione non valida");
        if(teamHackathonService.checkIscrizioneHackathon(segnalazione.getTeam().getId(),segnalazione.getHackathon().getId())){
            teamHackathonService.rimuoviTeam(segnalazione.getTeam().getId(),segnalazione.getHackathon().getId());
            segnalazione.setStato(StatoSegnalazione.GESTITA);
            repository.save(segnalazione);
            return true;
        }else
        {
            segnalazione.setStato(StatoSegnalazione.GESTITA);
            repository.save(segnalazione);
            return false;
        }
    }

    /**
     * Rifiuta una segnalazione
     * @param segnalazione segnalazione da rifiutare
     * @return true se la segnalazione viene rifiutata
     */
    public boolean rifiutaSegnalazione(Segnalazione segnalazione){
        segnalazione.setStato(StatoSegnalazione.RIFIUTATA);
        repository.save(segnalazione);
        return true;
    }

    public Segnalazione getSegnalazioneById(long id){return repository.findById(id).orElse(null);}

    /**
     * Restituisce la lista delle segnalazioni di un determinato organizzatore
     * @param organizzatore organizzatore che ha segnalato
     * @param hackathon hackathon relativo alle segnalazioni
     * @param stato stato delle segnalazioni
     * @return lista delle segnalazioni
     */
    public List<Segnalazione> getSegnalazioni(Account organizzatore, List<Hackathon> hackathon, StatoSegnalazione stato){
        if (organizzatore == null)throw new NullPointerException("Organizzatore non valido");
        if (hackathon==null || hackathon.isEmpty()) throw new IllegalArgumentException("Hackathon non valido");
        if (stato==null) throw new NullPointerException("Stato non valido");
        return repository.findAll().stream().filter(s -> hackathon.contains(s.getHackathon())
                && s.getHackathon().getOrganizzatore().equals(organizzatore) && s.getStato() == stato).toList();


    }
}

package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.SegnalazioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SegnalazioneService {
    private final SegnalazioneRepository repository;
    private final TeamService teamService;
    private final HackathonService hackathonService;
    private final MentoreService mentoreService;
    private final TeamHackathonService teamHackathonService;

    public SegnalazioneService(SegnalazioneRepository repository, TeamService teamService, HackathonService hackathonService, MentoreService mentoreService, TeamHackathonService teamHackathonService) {
        this.repository = repository;
        this.teamService = teamService;
        this.hackathonService = hackathonService;
        this.mentoreService = mentoreService;
        this.teamHackathonService = teamHackathonService;
    }


    /**
     * Verifica se la segnalazione è già stata effettuata
     * @param team team da segnalare
     * @param hackathon hackathon relativo alla segnalazione
     * @param mentore mentore che ha segnalato
     * @param motivazione motivazione della segnalazione
     * @return true se la segnalazione va bene
     */
    public boolean verificaSegnalazione(Team team,Hackathon hackathon, Mentore mentore, String motivazione){
        if (team == null) throw new IllegalArgumentException("Team non valido");
        if (motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
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
        if (motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
        if(idHackathon < 0) throw new IllegalArgumentException("Id hackathon non valido");
        if(idMentore < 0) throw new IllegalArgumentException("Id mentore non valido");

        Team team=teamService.getTeamById(idTeam);
        // Recupera hackathon e mentore dalle rispettive persistence
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        Mentore mentore = mentoreService.getMentoreById(idMentore);
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
}

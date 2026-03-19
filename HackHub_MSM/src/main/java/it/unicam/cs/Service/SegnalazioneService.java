package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.time.LocalDateTime;

public class SegnalazioneService {
    private static SegnalazioneService service;
    private final StandardPersistence<Segnalazione> persistence;
    TeamService teamService;

    private SegnalazioneService(){
        this.persistence = new StandardPersistence<>(Segnalazione.class);
        this.teamService = TeamService.getInstance();
    }
    public static SegnalazioneService getInstance(){
        if(service == null)
            service = new SegnalazioneService();
        return service;
    }

    /**
     * Verifica se la segnalazione è già stata effettuata
     * @param team team da segnalare
     * @param idHackathon id dell'hackathon relativo alla segnalazione
     * @param idMentore id del mentore che ha segnalato
     * @param motivazione motivazione della segnalazione
     * @return true se la segnalazione va bene
     */
    public boolean verificaSegnalazione(Team team,long idHackathon, long idMentore, String motivazione){
        if (team == null) throw new IllegalArgumentException("Team non valido");
        if (motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
        //controllo che non ci sia già una segnalazione
        return persistence.getAll().stream()
                .anyMatch(s -> s.getTeam().equals(team)
                        && s.getHackathon().getId() == idHackathon
                        && s.getMentore().getId().equals(idMentore));
    }

    /**
     * Segnala un team
     * @param team team da segnalare
     * @param idHackathon id dell'hackathon relativo alla segnalazione
     * @param idMentore id del mentore che ha segnalato
     * @param motivazione motivazione della segnalazione
     * @return true se la segnalazione è andata a buon fine, false altrimenti
     */
    public boolean segnalaTeam(Team team,long idHackathon, long idMentore, String motivazione){
        if (team == null) throw new IllegalArgumentException("Team non valido");
        if (motivazione.isEmpty()) throw new IllegalArgumentException("Motivazione non valida");
        if(idHackathon < 0) throw new IllegalArgumentException("Id hackathon non valido");
        if(idMentore < 0) throw new IllegalArgumentException("Id mentore non valido");

        // Recupera hackathon e mentore dalle rispettive persistence
        Hackathon hackathon = HackathonService.getInstance().getHackathonByID(idHackathon);
        Mentore mentore = MentoreService.getInstance().getMentoreById(idMentore);
        if (hackathon == null || mentore == null) {
            return false;
        }
        Segnalazione segnalazione = new Segnalazione(StatoSegnalazione.DA_GESTIRE,LocalDateTime.now(),motivazione,
                team, mentore, hackathon);
        persistence.create(segnalazione);
        return true;

    }

    /**
     * Accetta una segnalazione
     * @param segnalazione segnalazione da accettare
     * @return true se l'accettazione
     */
    public boolean accettaSegnalazione(Segnalazione segnalazione){
        if (segnalazione==null) throw new NullPointerException("Segnalazione non valida");
        if(teamService.checkIscrizioneHackathon(segnalazione.getTeam().getId(),segnalazione.getHackathon().getId())){
            teamService.rimuoviTeam(segnalazione.getTeam().getId(),segnalazione.getHackathon().getId());
            segnalazione.setStato(StatoSegnalazione.GESTITA);
            persistence.update(segnalazione);
            return true;
        }else
        {
            segnalazione.setStato(StatoSegnalazione.GESTITA);
            persistence.update(segnalazione);
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
        persistence.update(segnalazione);
        return true;
    }
}

package it.unicam.cs.service;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Team;
import it.unicam.cs.model.TeamHackathon;
import it.unicam.cs.repository.TeamHackathonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Service per la gestione delle associazioni tra team e hackathon nel sistema HackHub.
 * Fornisce operazioni di iscrizione, disiscrizione, verifica e rimozione dei team dagli hackathon.
 */
@Service
@Transactional
public class TeamHackathonService {
    private final TeamHackathonRepository repository;
    private final MembroTeamService membroTeamService;
    /**
     * Costruisce un'istanza di {@code TeamHackathonService} con le dipendenze necessarie.
     *
     * @param repository        repository per l'accesso alle associazioni team-hackathon
     * @param membroTeamService service per la gestione dei membri del team
     */
    public TeamHackathonService(TeamHackathonRepository repository,
                                MembroTeamService membroTeamService) {
        this.repository = repository;
        this.membroTeamService = membroTeamService;
    }


    /**
     * Restituisce tutti gli hackathon a cui un team è iscritto.
     *
     * @param team Team di cui verificare le iscrizioni
     * @return Lista di {@link Hackathon} a cui il team è iscritto,
     *         o lista vuota se il team è null o non ha iscrizioni
     */
    public List<Hackathon> isIscrittoHackathon(Team team) {
        if (team == null || team.getHackathonIscritti() == null) {
            return new ArrayList<>();
        }

        return team.getHackathonIscritti().stream()
                .filter(TeamHackathon::isIscritto)
                .map(TeamHackathon::getHackathon)
                .collect(Collectors.toList());
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
        if (checkIscrizioneHackathon(team.getId(), hackathon.getId())) {
            return false;
        }
        if (hackathon.getScadenzaIscrizione().isBefore(LocalDateTime.now())) {
            return false;
        }
        int maxMembri = hackathon.getDimensioneMassimoTeam();
        if (membroTeamService.getMembri(team.getId()).size() > maxMembri) {
            return false;
        }
        if (membroTeamService.getMembri(team.getId()).isEmpty()) {
            return false;
        }
        TeamHackathon teamHackathon = new TeamHackathon(team, hackathon);
        repository.save(teamHackathon);
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
     * Verifica se un team è iscritto a un determinato hackathon.
     *
     * @param idTeam      ID del team; se {@code null} o non positivo restituisce {@code false}
     * @param idHackathon ID dell'hackathon; se non positivo restituisce {@code false}
     * @return {@code true} se il team è iscritto all'hackathon, {@code false} altrimenti
     */
    public boolean checkIscrizioneHackathon(Long idTeam, long idHackathon) {
        // Validazione input
        if (idTeam == null || idTeam <= 0 || idHackathon <= 0) {
            return false;
        }
        // Query ottimizzata: restituisce boolean senza caricare entità
        TeamHackathon teamHackathon = repository.findByTeamIdAndHackathonId(idTeam, idHackathon);
        if(teamHackathon==null)
            return false;
        else return teamHackathon.isIscritto();


    }
    /**
     * Rimuove un team da un hackathon impostando il flag di iscrizione a {@code false}.
     *
     * @param idTeam      ID del team da rimuovere; deve essere maggiore o uguale a zero
     * @param idHackathon ID dell'hackathon; deve essere maggiore o uguale a zero
     * @return {@code true} se la rimozione è avvenuta con successo, {@code false} se
     *         l'iscrizione non è stata trovata
     * @throws IllegalArgumentException se gli ID sono negativi
     */
    public boolean rimuoviTeam(long idTeam, long idHackathon){
        if(idTeam < 0) throw new IllegalArgumentException("Team non valido");
        if(idHackathon < 0) throw new IllegalArgumentException("Hackathon non valido");

        TeamHackathon teamHackathon = repository.findAll().stream()
                .filter(th -> th.getTeam().getId().equals(idTeam)
                        && th.getHackathon().getId() == idHackathon)
                .findFirst()
                .orElse(null);

        if (teamHackathon == null) {
            return false; // Iscrizione non trovata
        }
        teamHackathon.setIscritto(false);
        repository.save(teamHackathon);
        return true;
    }

    /**
     * Disiscrive un team da un hackathon.
     * Verifica che l'iscrizione esista e che il team sia ancora iscritto prima di procedere.
     *
     * @param idTeam      ID del team da disiscrivere; deve essere positivo e non {@code null}
     * @param idHackathon ID dell'hackathon; deve essere positivo e non {@code null}
     * @return {@code true} se la disiscrizione è avvenuta con successo, {@code false} se
     *         l'iscrizione non esiste o il team non era più iscritto
     * @throws IllegalArgumentException se gli ID sono {@code null} o non positivi
     */
    public boolean disiscrivereTeam(Long idTeam, Long idHackathon){
        if (idHackathon == null || idHackathon <= 0) throw new IllegalArgumentException("Hackathon non valido.");
        if (idTeam == null || idTeam <= 0) throw new IllegalArgumentException("Team non valido.");

        TeamHackathon teamHackathon = repository.findByTeamIdAndHackathonId(idTeam,idHackathon);
        if (teamHackathon == null) return false;
        if (!teamHackathon.isIscritto()) return false;
        teamHackathon.setIscritto(false);
        repository.save(teamHackathon);
        return true;
    }
}

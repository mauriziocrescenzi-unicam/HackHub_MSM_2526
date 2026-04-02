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

@Service
@Transactional
public class TeamHackathonService {
    private final TeamHackathonRepository repository;
    private final MembroTeamService membroTeamService;

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

        if (!isIscrittoHackathon(team).isEmpty()) {
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

    public boolean checkIscrizioneHackathon(Long idTeam, long idHackathon) {
        if(idHackathon <0) throw new NullPointerException("Hackathon non valido");
        if(idTeam <0) throw new NullPointerException("Team non valido");
        return repository.findAll().stream()
                .anyMatch(th -> th.getTeam().getId().equals(idTeam)
                        && th.getHackathon().getId() ==idHackathon);

    }

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

        repository.delete(teamHackathon);
        return true;
    }

    /**
     * Metodo per disiscrivere un team da un hackathon
     * @param idTeam
     * @param idHackathon
     * @return
     */
    public boolean disiscrivereTeam(Long idTeam, Long idHackathon){
        if (idHackathon == null || idHackathon <= 0) throw new IllegalArgumentException("Hackathon non valido.");
        if (idTeam == null || idTeam <= 0) throw new IllegalArgumentException("Team non valido.");

        TeamHackathon teamHackathon = repository.findByTeamIdAndHackathonId(idTeam,idHackathon);
        if (teamHackathon == null) return false;
        repository.delete(teamHackathon);
        return true;
    }
}

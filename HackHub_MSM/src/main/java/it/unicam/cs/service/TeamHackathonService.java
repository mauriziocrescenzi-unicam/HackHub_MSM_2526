package it.unicam.cs.service;

import it.unicam.cs.model.TeamHackathon;
import it.unicam.cs.repository.TeamHackathonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamHackathonService {
    private final TeamHackathonRepository repository;

    public TeamHackathonService(TeamHackathonRepository repository) {
        this.repository = repository;
    }

    public boolean disiscriviTeam(Long idTeam,Long idHackathon){
        if (idHackathon == null || idHackathon <= 0) throw new IllegalArgumentException("Hackathon non valido.");
        if (idTeam == null || idTeam <= 0) throw new IllegalArgumentException("Team non valido.");

        TeamHackathon teamHackathon = repository.findByTeamIdAndHackathonId(idTeam,idHackathon);
        if (teamHackathon == null) return false;
        repository.delete(teamHackathon);
        return true;
    }
}

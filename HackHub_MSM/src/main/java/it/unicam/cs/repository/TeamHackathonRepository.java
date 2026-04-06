package it.unicam.cs.repository;

import it.unicam.cs.model.Team;
import it.unicam.cs.model.TeamHackathon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamHackathonRepository extends JpaRepository<TeamHackathon,Long> {
    TeamHackathon findByTeamIdAndHackathonId(Long idTeam, Long idHackathon);

    boolean existsByTeamIdAndHackathonId(Long teamId, Long hackathonId);

}

package it.unicam.cs.repository;

import it.unicam.cs.model.TeamHackathon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamHackathonRepository extends JpaRepository<TeamHackathon,Long> {
    TeamHackathon findByTeamIdAndHackathonId(Long idTeam, Long idHackathon);

    List<TeamHackathon> findByTeamId(Long idTeam);

}

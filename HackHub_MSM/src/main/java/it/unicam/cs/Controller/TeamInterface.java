package it.unicam.cs.Controller;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Team;
import java.util.List;

public interface TeamInterface {
    TeamController controller = null;
    
    boolean creaTeam();
    List<Hackathon> getHackathonDisponibili();
    boolean iscrivereTeam(Hackathon hackathon, Team team);
}
package it.unicam.cs.controller;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Team;
import it.unicam.cs.service.TeamService;
import it.unicam.cs.service.HackathonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final HackathonService hackathonService;


    public TeamController(TeamService teamService, HackathonService hackathonService) {
        this.teamService = teamService;
        this.hackathonService = hackathonService;
    }
    @PostMapping
    public ResponseEntity<String> createTeam(@RequestBody Map<String, Object> teamData) {
        String nome = (String) teamData.get("nome");
        Object utenteIdRaw = teamData.get("utenteId");
        if (nome == null || nome.trim().isEmpty() || utenteIdRaw == null) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        String descrizione = (String) teamData.get("descrizione");
        Long utenteId = ((Number) utenteIdRaw).longValue();
        if(teamService.creaTeam(nome, descrizione, utenteId))
            return ResponseEntity.status(HttpStatus.CREATED).body("Team creato con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }

    @PostMapping("/{idTeam}/hackathons/{idHackathon}")
    public ResponseEntity<String> iscrivereTeam(@PathVariable Long idTeam, @PathVariable long idHackathon) {
        Team team = teamService.getTeamById(idTeam);
        if (team == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team non trovato");
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");
        if (teamService.iscrivereTeam(hackathon, team))
            return ResponseEntity.status(HttpStatus.CREATED).body("Team iscritto con successo");
        return ResponseEntity.badRequest().body("Iscrizione non riuscita");
    }
}

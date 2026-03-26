package it.unicam.cs.controller;

import it.unicam.cs.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {this.teamService = teamService;}

    @PostMapping
    public ResponseEntity<String> createTeam(@RequestBody Map<String, Object> teamData) {
        String nome = (String) teamData.get("nome");
        String descrizione= (String) teamData.get("descrizione");
        Long utenteId = ((Number) teamData.get("utenteId")).longValue();
        teamService.creaTeam(nome, descrizione, utenteId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Team creato con successo");
    }
}

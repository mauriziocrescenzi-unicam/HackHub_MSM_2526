package it.unicam.cs.controller;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Team;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.TeamService;
import it.unicam.cs.service.HackathonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final AccountService accountService;


    public TeamController(TeamService teamService, AccountService accountService) {
        this.teamService = teamService;
        this.accountService = accountService;
    }
    @PostMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> createTeam(@RequestBody Map<String, Object> teamData, Authentication auth) {
        String nome = (String) teamData.get("nome");
        if (nome == null || nome.trim().isEmpty() ) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        String descrizione = (String) teamData.get("descrizione");
        Long utenteId = accountService.findId(auth.getName());
        if(teamService.creaTeam(nome, descrizione, utenteId))
            return ResponseEntity.status(HttpStatus.CREATED).body("Team creato con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }

}

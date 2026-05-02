package it.unicam.cs.controller;

import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * Controller REST per la gestione dei team.
 * Espone l'endpoint per la creazione di un nuovo team.
 * Accessibile solo agli utenti con ruolo {@code UTENTE}.
 */
@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final AccountService accountService;

    /**
     * Costruisce un'istanza di {@code TeamController} con le dipendenze necessarie.
     *
     * @param teamService    service per la gestione dei team
     * @param accountService service per la gestione degli account
     */
    public TeamController(TeamService teamService, AccountService accountService) {
        this.teamService = teamService;
        this.accountService = accountService;
    }
    /**
     * {@code POST /teams}
     * Crea un nuovo team con il nome e la descrizione forniti.
     * L'utente autenticato viene automaticamente aggiunto come primo membro del team.
     *
     * @param teamData il body della richiesta contenente {@code nome} (String, obbligatorio)
     *                 e {@code descrizione} (String, opzionale)
     * @param auth     il contesto di autenticazione corrente
     * @return {@code 201 Created} se il team è stato creato con successo;
     *         {@code 400 Bad Request} se il nome è assente o l'utente è già in un team
     */
    @PostMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> createTeam(@RequestBody Map<String, Object> teamData, Authentication auth) {
        String nome = (String) teamData.get("nome");
        if (nome == null || nome.trim().isEmpty() ) {
            return ResponseEntity.badRequest().body("Non sono stati inseriti i dati richiesti");
        }
        String descrizione = (String) teamData.get("descrizione");
        Long utenteId = accountService.findId(auth.getName());
        if(teamService.creaTeam(nome, descrizione, utenteId))
            return ResponseEntity.status(HttpStatus.CREATED).body("Team creato con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }

}

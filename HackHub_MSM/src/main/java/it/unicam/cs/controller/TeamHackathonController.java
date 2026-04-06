package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Team;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.TeamHackathonService;
import it.unicam.cs.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per la gestione delle iscrizioni dei team agli hackathon.
 * Espone endpoint per iscrivere, verificare e disiscrivere team dagli hackathon.
 */
@RestController
@RequestMapping("/team")
public class TeamHackathonController {

    private final TeamHackathonService teamHackathonService;
    private final TeamService teamService;
    private final HackathonService hackathonService;

    public TeamHackathonController(TeamHackathonService teamHackathonService,
                                   TeamService teamService,
                                   HackathonService hackathonService) {
        this.teamHackathonService = teamHackathonService;
        this.teamService = teamService;
        this.hackathonService = hackathonService;
    }

    @GetMapping("/{idTeam}/hackathons")
    public ResponseEntity<List<HackathonRispostaDTO>> isIscrittoHackathon(@PathVariable long idTeam) {
        Team team = teamService.getTeamById(idTeam);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        List<Hackathon> hackathons = teamHackathonService.isIscrittoHackathon(team);
        if (hackathons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<HackathonRispostaDTO> risposta = hackathons.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/{idTeam}/hackathons/{idHackathon}/iscritto")
    public ResponseEntity<Boolean> checkIscrizioneHackathon(@PathVariable long idTeam, @PathVariable long idHackathon) {
        boolean iscritto = teamHackathonService.checkIscrizioneHackathon(idTeam, idHackathon);
        return ResponseEntity.ok(iscritto);
    }

    @PostMapping("/{idTeam}/hackathons/{idHackathon}/iscriviti")
    public ResponseEntity<String> iscrivereTeam(@PathVariable long idTeam, @PathVariable long idHackathon) {
        Team team = teamService.getTeamById(idTeam);
        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team non trovato");
        }
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");
        }
        // Verifica che l'hackathon sia in stato IN_ISCRIZIONE
        if (hackathon.getStato() != it.unicam.cs.model.StatoHackathon.IN_ISCRIZIONE) {
            return ResponseEntity.badRequest().body("Iscrizioni non aperte per questo hackathon");
        }
        boolean iscritto = teamHackathonService.iscrivereTeam(hackathon, team);
        if (!iscritto) {
            return ResponseEntity.badRequest().body("Iscrizione non riuscita. Verificare i requisiti.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Team iscritto con successo");
    }

    @DeleteMapping("/{idTeam}/hackathons/{idHackathon}/disiscriviti")
    public ResponseEntity<String> disiscrivitiHackathon(@PathVariable long idTeam, @PathVariable long idHackathon) {
        boolean disiscritto = teamHackathonService.disiscrivereTeam(idTeam, idHackathon);
        if (!disiscritto) {
            return ResponseEntity.badRequest().body("Disiscrizione non riuscita. Verificare che il team sia iscritto.");
        }
        return ResponseEntity.ok("Team disiscritto con successo");
    }
}
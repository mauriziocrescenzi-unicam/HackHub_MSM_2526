package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.Account;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.MembroTeam;
import it.unicam.cs.model.Team;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final MembroTeamService membroTeamService;
    private final AccountService accountService;

    public TeamHackathonController(TeamHackathonService teamHackathonService,
                                   TeamService teamService,
                                   HackathonService hackathonService,
                                   AccountService accountService,
                                   MembroTeamService membroTeamService) {
        this.teamHackathonService = teamHackathonService;
        this.teamService = teamService;
        this.hackathonService = hackathonService;
        this.membroTeamService = membroTeamService;
        this.accountService = accountService;
    }

    /**
     * GET /team/hackathons/lista
     * Restituisce la lista degli hackathon a cui il team dell'utente loggato è iscritto.
     * L'idTeam viene recuperato automaticamente dall'account autenticato.
     */
    @GetMapping("/hackathons/lista")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<List<HackathonRispostaDTO>> isIscrittoHackathon(Authentication auth) {
        // 1. Recupera l'account dall'utente autenticato
        Account account = accountService.find(auth.getName());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 2. Recupera il membro e il team (con controllo null-safe)
        MembroTeam membro = membroTeamService.getMembroById(account.getId());
        if (membro == null || membro.getTeam() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long idTeam = membro.getTeam().getId();
        // 3. Recupera il team e gli hackathon iscritti
        Team team = teamService.getTeamById(idTeam);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        List<Hackathon> hackathons = teamHackathonService.isIscrittoHackathon(team);
        if (hackathons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // 4. Converti in DTO e restituisci
        List<HackathonRispostaDTO> risposta = hackathons.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * POST /team/hackathons/iscritto
     * Verifica se il team è iscritto a un hackathon specifico.
     * Body: { "idTeam": 1, "idHackathon": 10 }
     */
    @PostMapping("/iscritto")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<Boolean> checkIscrizioneHackathon(
            @RequestBody Map<String, Long> body,
            Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long idTeam = membroTeamService.getMembroById(account.getId()).getTeam().getId();
        Long idHackathon = body.get("idHackathon");
        MembroTeam membro = membroTeamService.getMembro(account);
        if (membro == null || !membro.getTeam().getId().equals(idTeam)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
        boolean iscritto = teamHackathonService.checkIscrizioneHackathon(idTeam, idHackathon);
        return ResponseEntity.ok(iscritto);
    }

    /**
     * POST /team/iscriviti
     * Iscrive un team a un hackathon.
     */
    @PostMapping("/iscriviti")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> iscrivereTeam(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idTeam = membroTeamService.getMembroById(account.getId()).getTeam().getId();
        Long idHackathon = body.get("idHackathon");
        MembroTeam membro = membroTeamService.getMembro(account);
        if (membro == null || !membro.getTeam().getId().equals(idTeam)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi iscrivere solo i tuoi team");
        }
        Team team = teamService.getTeamById(idTeam);
        if (team == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team non trovato");
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");
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

    @DeleteMapping("/hackathons/{idHackathon}")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> disiscrivitiHackathon(@PathVariable Long idHackathon, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        MembroTeam membro = membroTeamService.getMembroById(account.getId());
        if (membro == null || membro.getTeam() == null) {
            return ResponseEntity.badRequest().body("Utente non appartiene a nessun team");
        }
        Long idTeam = membro.getTeam().getId();
        boolean disiscritto = teamHackathonService.disiscrivereTeam(idTeam, idHackathon);
        if (!disiscritto) {
            return ResponseEntity.badRequest().body("Disiscrizione non riuscita. Verificare che il team sia iscritto.");
        }
        return ResponseEntity.ok("Team disiscritto con successo");
    }
}
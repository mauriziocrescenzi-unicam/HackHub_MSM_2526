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
 * Espone endpoint per iscrivere, verificare lo stato di iscrizione
 * e disiscrivere un team da un hackathon.
 * Accessibile solo agli utenti con ruolo {@code UTENTE}.
 */
@RestController
@RequestMapping("/team")
public class TeamHackathonController {

    private final TeamHackathonService teamHackathonService;
    private final TeamService teamService;
    private final HackathonService hackathonService;
    private final MembroTeamService membroTeamService;
    private final AccountService accountService;
    /**
     * Costruisce un'istanza di {@code TeamHackathonController} con le dipendenze necessarie.
     *
     * @param teamHackathonService service per la gestione delle iscrizioni team-hackathon
     * @param teamService          service per la gestione dei team
     * @param hackathonService     service per la gestione degli hackathon
     * @param accountService       service per la gestione degli account
     * @param membroTeamService    service per la gestione dei membri del team
     */
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
     * {@code GET /team/hackathons/lista}
     * Restituisce la lista degli hackathon a cui è iscritto il team dell'utente autenticato.
     * Il team viene ricavato automaticamente dall'account autenticato.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista degli hackathon a cui il team è iscritto;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è membro di nessun team;
     *         {@code 404 Not Found} se il team non esiste o non ha iscrizioni attive
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
     * {@code GET /team/iscritto}
     * Verifica se il team dell'utente autenticato è iscritto a un hackathon specifico.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param idHackathon l'id del hackathon da verificare
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con {@code true} se il team è iscritto, {@code false} altrimenti;
     *         {@code 400 Bad Request} se {@code idHackathon} è assente;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è membro di nessun team
     */
    @GetMapping("/iscritto")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<Boolean> checkIscrizioneHackathon(
            @RequestParam Long idHackathon,
            Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!membroTeamService.isMembroTeam(account.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Long idTeam = membroTeamService.getMembroById(account.getId()).getTeam().getId();
        if (idHackathon == null) return ResponseEntity.badRequest().body(false);
        MembroTeam membro = membroTeamService.getMembro(account);
        if (membro == null || !membro.getTeam().getId().equals(idTeam)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
        boolean iscritto = teamHackathonService.checkIscrizioneHackathon(idTeam, idHackathon);
        return ResponseEntity.ok(iscritto);
    }

    /**
     * {@code POST /team/iscriviti}
     * Iscrive il team dell'utente autenticato a un hackathon.
     * L'hackathon deve essere in stato {@link it.unicam.cs.model.StatoHackathon #IN_ISCRIZIONE}.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param body il body della richiesta contenente {@code idHackathon} (Long)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 201 Created} se l'iscrizione è avvenuta con successo;
     *         {@code 400 Bad Request} se le iscrizioni non sono aperte o i requisiti non sono soddisfatti;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è membro di nessun team;
     *         {@code 404 Not Found} se il team o l'hackathon non vengono trovati
     */
    @PostMapping("/iscriviti")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> iscrivereTeam(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        if (!membroTeamService.isMembroTeam(account.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
    /**
     * {@code DELETE /team/hackathons/{idHackathon}}
     * Disiscrive il team dell'utente autenticato da un hackathon specifico.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param idHackathon l'ID dell'hackathon da cui disiscriversi
     * @param auth        il contesto di autenticazione corrente
     * @return {@code 200 OK} se la disiscrizione è avvenuta con successo;
     *         {@code 400 Bad Request} se l'utente non è in un team o la disiscrizione fallisce;
     *         {@code 401 Unauthorized} se l'account non è trovato
     */
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
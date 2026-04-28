package it.unicam.cs.controller;

import it.unicam.cs.dto.*;
import it.unicam.cs.model.*;
import it.unicam.cs.repository.HackathonRepository;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/membro-team")
public class MembroTeamController {

    private final MembroTeamService membroTeamService;
    private final TeamHackathonService teamHackathonService;
    private final HackathonService hackathonService;
    private final SottomissioneService sottomissioneService;
    private final TeamService teamService;
    private final RichiestaSupportoService richiestaSupportoService;
    private final HackathonRepository hackathonRepository;
    private final AccountService accountService;

    public MembroTeamController(MembroTeamService membroTeamService,
                                TeamHackathonService teamHackathonService,
                                HackathonService hackathonService,
                                SottomissioneService sottomissioneService,
                                TeamService teamService, RichiestaSupportoService richiestaSupportoService,
                                HackathonRepository hackathonRepository,
                                AccountService accountService) {
        this.membroTeamService = membroTeamService;
        this.teamHackathonService = teamHackathonService;
        this.hackathonService = hackathonService;
        this.sottomissioneService = sottomissioneService;
        this.teamService = teamService;
        this.richiestaSupportoService = richiestaSupportoService;
        this.hackathonRepository = hackathonRepository;
        this.accountService = accountService;
    }

    @GetMapping("/team/membri")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<List<MembroTeamRispostaDTO>> getMembri(Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!membroTeamService.isMembroTeam(account.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long idTeam = membroTeamService.getMembroById(account.getId()).getTeam().getId();
        List<MembroTeam> membri = membroTeamService.getMembri(idTeam);
        if (membri.isEmpty()) return ResponseEntity.notFound().build();
        List<MembroTeamRispostaDTO> risposta = membri.stream()
                .map(MembroTeamRispostaDTO::fromMembroTeam)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @DeleteMapping("/abbandona")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> abbandonaTeam(Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        }
        Long idMembro = account.getId();
        MembroTeam membro = membroTeamService.getMembroById(idMembro);
        if (membro == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Utente non appartiene a nessun team");
        }
        Long idTeam = membro.getTeam().getId();
        boolean abbandonato = membroTeamService.abbandonaTeam(idMembro, idTeam);
        if (!abbandonato) {
            return ResponseEntity.badRequest().body("Abbandono non riuscito. Verificare che il membro appartenga al team.");
        }
        return ResponseEntity.ok("Membro ha abbandonato il team con successo");
    }

    /**
     * POST /membro-team/elimina
     * Permette a un membro del team di eliminare un altro membro dallo stesso team.
     * Body: { "idMembroCheElimina": 1, "idMembroDaEliminare": 2, "idTeam": 5 }
     */
    @PostMapping("/elimina")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> eliminaMembro(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMembroCheElimina = account.getId();
        Long idMembroDaEliminare = body.get("idMembroDaEliminare");
        Long idTeam = membroTeamService.getMembroById(idMembroCheElimina).getTeam().getId();
        if (!account.getId().equals(idMembroCheElimina)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi eliminare solo membri del tuo team");
        }
        if (idMembroCheElimina.equals(idMembroDaEliminare)) {
            return ResponseEntity.badRequest().body("Impossibile: un membro non può eliminare se stesso dal team, in tal caso abbandonare il team.");
        }
        boolean eliminato = membroTeamService.eliminaMembro(idMembroCheElimina, idMembroDaEliminare, idTeam);
        if (!eliminato) {
            return ResponseEntity.badRequest().body("Eliminazione non riuscita. Verificare che entrambi i membri appartengano al team.");
        }
        return ResponseEntity.ok("Membro eliminato dal team con successo");
    }

    @PostMapping("/supporto/richiedi")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> inviaRichiestaSupporto(
            @RequestBody RichiestaSupportoInvioDTO dto,
            Authentication auth) {
        // 1. Recupera l'account dall'utente autenticato
        Account account = accountService.find(auth.getName());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        }
        // 2. Recupera il membro e il team (con controllo null-safe)
        MembroTeam membro = membroTeamService.getMembroById(account.getId());
        if (membro == null || membro.getTeam() == null) {
            return ResponseEntity.badRequest().body("Utente non appartiene a nessun team");
        }
        Long idTeam = membro.getTeam().getId();
        // 3. Recupera l'hackathon
        Hackathon hackathon = hackathonRepository.findById(dto.idHackathon()).orElse(null);
        if (hackathon == null) {
            return ResponseEntity.badRequest().body("Hackathon non trovato");
        }
        // 4. Imposta la data di invio automaticamente a now()
        LocalDateTime dataInvio = LocalDateTime.now();
        // 5. Invia la richiesta di supporto
        RichiestaSupporto richiesta = richiestaSupportoService.inviaRichiestaSupporto(
                idTeam,                      // ← preso dal team dell'utente loggato
                dto.descrizioneRichiesta(),  // ← dal DTO
                dataInvio,                   // ← impostato automaticamente
                hackathon                    // ← recuperato dal DB
        );
        if (richiesta == null) {
            return ResponseEntity.badRequest().body("Validazione richiesta fallita");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Richiesta di supporto inviata con successo!");
    }
}

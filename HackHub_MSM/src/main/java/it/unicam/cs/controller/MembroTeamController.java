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


    /**
     * POST /membro-team/team/membri
     * Restituisce la lista dei membri di un team specifico.
     * Body: { "idTeam": 5 }
     */
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

    //TODO controllare il tipo di richiesta
    /**
     * POST /membro-team/abbandona
     * Gestisce l'abbandono volontario di un membro da un team.
     * Body: { "idMembro": 1, "idTeam": 5 }
     */
    @DeleteMapping("/abbandona")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> abbandonaTeam(Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMembro = account.getId();
        Long idTeam = membroTeamService.getMembroById(idMembro).getTeam().getId();
        if (!account.getId().equals(idMembro)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Puoi abbandonare solo il tuo team");
        }
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

    /**
     * POST /membro-team/supporto/richiedi
     * Invia una richiesta di supporto.
     * Body: { "idMembroTeam": 1, "descrizioneRichiesta": "...", "dataInvio": "2026-04-10T14:30:00", "idHackathon": 10 }
     */
    @PostMapping("/supporto/richiedi")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> inviaRichiestaSupporto(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        // Estrazione dati dalla Map (richiede cast perché i valori sono Object)
        String descrizioneRichiesta = (String) body.get("descrizioneRichiesta");
        Long idHackathon = ((Number) body.get("idHackathon")).longValue();
        // Gestione della data: Spring di solito deserializza le date come stringhe nelle Map
        LocalDateTime dataInvio = null;
        Object dataObj = body.get("dataInvio");
        if (dataObj instanceof String) {
            dataInvio = LocalDateTime.parse((String) dataObj);
        } else if (dataObj instanceof LocalDateTime) {
            dataInvio = (LocalDateTime) dataObj;
        }
        Hackathon hackathon = hackathonRepository.findById(idHackathon).orElse(null);
        if (hackathon == null) return ResponseEntity.badRequest().body("Hackathon non trovato");
        RichiestaSupporto richiesta = richiestaSupportoService.inviaRichiestaSupporto(
                membroTeamService.getMembroById(account.getId()).getTeam().getId(),
                descrizioneRichiesta,
                dataInvio,
                hackathon
        );
        if (richiesta == null) {
            return ResponseEntity.badRequest().body("Validazione richiesta fallita");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Richiesta di supporto inviata con successo!");
    }
}

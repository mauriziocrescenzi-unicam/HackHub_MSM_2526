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

    // ==================== ISCRIZIONE HACKATHON ====================


    /**
     * POST /membro-team/hackathons/iscritto
     * Restituisce la lista degli hackathon a cui il team del membro è iscritto.
     * ID passato nel body: { "idMembro": 1 }
     */
    @PostMapping("/hackathons/iscritto")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<HackathonRispostaDTO>> isIscrittoHackathon(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        Long idMembro = body.get("idMembro");
        if (account == null || !account.getId().equals(idMembro)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Team team = teamService.getTeamByMembroId(idMembro);
        if (team == null) return ResponseEntity.notFound().build();
        List<Hackathon> hackathons = teamHackathonService.isIscrittoHackathon(team);
        if (hackathons.isEmpty()) return ResponseEntity.notFound().build();
        List<HackathonRispostaDTO> risposta = hackathons.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * POST /membro-team/hackathons/stato
     * Verifica se almeno un hackathon del team è in uno degli stati specificati.
     * Body: { "idMembro": 1, "stati": ["IN_ISCRIZIONE", "CONCLUSO"] }
     */
    @PostMapping("/hackathons/stato")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> checkStato(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        Long idMembro = ((Number) body.get("idMembro")).longValue();
        if (account == null || !account.getId().equals(idMembro)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
        if (body.get("stati") == null) return ResponseEntity.badRequest().body(null);
        List<?> statiRaw = (List<?>) body.get("stati");
        StatoHackathon[] stati = statiRaw.stream()
                .map(s -> StatoHackathon.fromString(s.toString()))
                .toArray(StatoHackathon[]::new);
        Team team = teamService.getTeamByMembroId(idMembro);
        if (team == null) return ResponseEntity.notFound().build();
        List<Hackathon> listaHackathon = teamHackathonService.isIscrittoHackathon(team);
        boolean risultato = hackathonService.checkStato(listaHackathon, stati);
        return ResponseEntity.ok(risultato);
    }

    // ==================== SOTTOMISSIONE ====================

    /**
     * POST /membro-team/sottomissione/presente
     * Verifica se esiste già una sottomissione per il team nell'hackathon.
     * Body: { "idMembro": 1, "idHackathon": 10 }
     */
    @PostMapping("/sottomissione/presente")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> isPresente(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        Long idMembro = body.get("idMembro");
        if (account == null || !account.getId().equals(idMembro)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
        Team team = teamService.getTeamByMembroId(idMembro);
        if (team == null) return ResponseEntity.notFound().build();
        Long idHackathon = body.get("idHackathon");
        boolean presente = sottomissioneService.isPresente(team.getId(), idHackathon);
        return ResponseEntity.ok(presente);
    }

    // ==================== GESTIONE MEMBRI TEAM ====================

    /**
     * POST /membro-team/team/membri
     * Restituisce la lista dei membri di un team specifico.
     * Body: { "idTeam": 5 }
     */
    @PostMapping("/team/membri")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MembroTeamRispostaDTO>> getMembri(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long idTeam = body.get("idTeam");
        if (!membroTeamService.isMembroTeam(account.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<MembroTeam> membri = membroTeamService.getMembri(idTeam);
        if (membri.isEmpty()) return ResponseEntity.notFound().build();
        List<MembroTeamRispostaDTO> risposta = membri.stream()
                .map(MembroTeamRispostaDTO::fromMembroTeam)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * POST /membro-team/abbandona
     * Gestisce l'abbandono volontario di un membro da un team.
     * Body: { "idMembro": 1, "idTeam": 5 }
     */
    @PostMapping("/abbandona")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> abbandonaTeam(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMembro = body.get("idMembro");
        Long idTeam = body.get("idTeam");
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> eliminaMembro(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMembroCheElimina = body.get("idMembroCheElimina");
        Long idMembroDaEliminare = body.get("idMembroDaEliminare");
        Long idTeam = body.get("idTeam");
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> inviaRichiestaSupporto(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        // Estrazione dati dalla Map (richiede cast perché i valori sono Object)
        Long idMembroTeam = ((Number) body.get("idMembroTeam")).longValue();
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
        // Verifica autorizzazione
        if (!account.getId().equals(idMembroTeam)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi inviare richieste solo per te stesso");
        }
        Hackathon hackathon = hackathonRepository.findById(idHackathon).orElse(null);
        if (hackathon == null) return ResponseEntity.badRequest().body("Hackathon non trovato");
        RichiestaSupporto richiesta = richiestaSupportoService.inviaRichiestaSupporto(
                idMembroTeam,
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

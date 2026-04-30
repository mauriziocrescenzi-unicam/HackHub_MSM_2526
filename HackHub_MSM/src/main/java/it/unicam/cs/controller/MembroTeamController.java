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

/**
 * Controller REST per le operazioni dei membri del team.
 * Espone endpoint per visualizzare i propri compagni di team, abbandonare o eliminare membri
 * e inviare richieste di supporto durante un hackathon.
 * Accessibile solo agli utenti con ruolo {@code UTENTE}.
 */
@RestController
@RequestMapping("/membro-team")
public class MembroTeamController {

    private final MembroTeamService membroTeamService;
    private final TeamHackathonService teamHackathonService;
    private final RichiestaSupportoService richiestaSupportoService;
    private final HackathonRepository hackathonRepository;
    private final AccountService accountService;
    /**
     * Costruisce un'istanza di {@code MembroTeamController} con le dipendenze necessarie.
     *
     * @param membroTeamService        service per la gestione dei membri del team
     * @param teamHackathonService     service per la gestione delle iscrizioni team-hackathon
     * @param richiestaSupportoService service per la gestione delle richieste di supporto
     * @param hackathonRepository      repository per l'accesso diretto agli hackathon
     * @param accountService           service per la gestione degli account
     */
    public MembroTeamController(MembroTeamService membroTeamService,
                                TeamHackathonService teamHackathonService,
                                RichiestaSupportoService richiestaSupportoService,
                                HackathonRepository hackathonRepository,
                                AccountService accountService) {
        this.membroTeamService = membroTeamService;
        this.teamHackathonService = teamHackathonService;
        this.richiestaSupportoService = richiestaSupportoService;
        this.hackathonRepository = hackathonRepository;
        this.accountService = accountService;
    }
    /**
     * {@code GET /membro-team/team/membri}
     * Restituisce la lista dei membri del team a cui appartiene l'utente autenticato.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista dei membri;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è membro di nessun team;
     *         {@code 404 Not Found} se il team non ha membri
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
    /**
     * {@code DELETE /membro-team/abbandona}
     * Permette all'utente autenticato di abbandonare il proprio team.
     * Se il team rimane senza membri, viene automaticamente eliminato.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se l'abbandono è avvenuto con successo;
     *         {@code 400 Bad Request} se l'utente non appartiene a nessun team o l'operazione fallisce;
     *         {@code 401 Unauthorized} se l'account non è trovato
     */
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
    //TODO cambiare in delete?
    /**
     * {@code POST /membro-team/elimina}
     * Permette a un membro del team di rimuovere un altro membro dallo stesso team.
     * Un membro non può eliminare se stesso; per uscire dal team deve usare l'endpoint di abbandono.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param body il body della richiesta contenente {@code idMembroDaEliminare} (Long)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se l'eliminazione è avvenuta con successo;
     *         {@code 400 Bad Request} se i dati non sono validi o l'operazione fallisce;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è membro di nessun team
     */
    @PostMapping("/elimina")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> eliminaMembro(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMembroCheElimina = account.getId();
        Long idMembroDaEliminare = body.get("idMembroDaEliminare");
        if(membroTeamService.getMembroById(idMembroCheElimina)==null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non puoi eliminare un membro");
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
     * {@code POST /membro-team/supporto/richiedi}
     * Invia una richiesta di supporto al mentore per l'hackathon specificato.
     * Il team richiedente viene ricavato dall'account autenticato.
     * L'hackathon deve essere in stato {@link StatoHackathon#IN_CORSO} e il team deve essere iscritto.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param dto  i dati della richiesta di supporto contenenti {@code idHackathon} e {@code descrizioneRichiesta}
     * @param auth il contesto di autenticazione corrente
     * @return {@code 201 Created} se la richiesta è stata inviata con successo;
     *         {@code 400 Bad Request} se i dati non sono validi, l'hackathon non è in corso
     *         o il team non è iscritto;
     *         {@code 401 Unauthorized} se l'account non è trovato
     */
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
        if(!teamHackathonService.checkIscrizioneHackathon(idTeam, hackathon.getId()))
            return ResponseEntity.badRequest().body("Non puoi richiedere supporto per questo hackathon");
        if (hackathon.getStato() != StatoHackathon.IN_CORSO)
            return ResponseEntity.badRequest().body("Non puoi richiedere supporto per questo hackathon");
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

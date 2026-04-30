package it.unicam.cs.controller;

import it.unicam.cs.dto.RichiestaSupportoRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * Controller REST per la gestione delle richieste di supporto e delle segnalazioni dei mentori.
 * Espone endpoint per visualizzare e rispondere alle richieste di supporto,
 * e per segnalare team in violazione del regolamento.
 * Accessibile solo agli utenti con ruolo {@code STAFF}.
 */
@RestController
@RequestMapping("/mentori")
public class RichiestaSupportoController {

    private final HackathonService hackathonService;
    private final SegnalazioneService segnalazioneService;
    private final AccountService accountService;
    private final MembroDelloStaffService membroDelloStaffService;
    private final RichiestaSupportoService richiestaSupportoService;
    /**
     * Costruisce un'istanza di {@code RichiestaSupportoController} con le dipendenze necessarie.
     *
     * @param hackathonService   service per la gestione degli hackathon
     * @param segnalazioneService service per la gestione delle segnalazioni
     * @param accountService     service per la gestione degli account
     */
    public RichiestaSupportoController(HackathonService hackathonService,
                                       SegnalazioneService segnalazioneService,
                                       AccountService accountService, MembroDelloStaffService membroDelloStaffService, RichiestaSupportoService richiestaSupportoService) {
        this.hackathonService = hackathonService;
        this.segnalazioneService = segnalazioneService;
        this.accountService = accountService;
        this.membroDelloStaffService = membroDelloStaffService;
        this.richiestaSupportoService = richiestaSupportoService;
    }


    //TODO cambiare in get
    /**
     * {@code POST /mentori/richieste/lista}
     * Restituisce le richieste di supporto per un hackathon specifico.
     * Il mentore autenticato deve essere effettivamente assegnato all'hackathon richiesto.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param body il body della richiesta contenente {@code idHackathon} (Long)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista delle richieste di supporto;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se il mentore non è assegnato all'hackathon;
     *         {@code 404 Not Found} se il mentore o l'hackathon non esistono,
     *         o se non ci sono richieste
     */
    @PostMapping("/richieste/lista")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<RichiestaSupportoRispostaDTO>> getRichiesteSupporto(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long idMentore = account.getId();
        Long idHackathon = body.get("idHackathon");
        // Verifica che il mentore esista
        if (membroDelloStaffService.getMembroStaffById(idMentore) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // Verifica che il mentore sia effettivamente assegnato a questo hackathon
        boolean assegnato = hackathon.getMentori().stream()
                .anyMatch(m -> m.getId().equals(idMentore));
        if (!assegnato) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        List<RichiestaSupporto> richieste = richiestaSupportoService.getRichiesteSupporto(hackathon);
        if (richieste.isEmpty()) return ResponseEntity.notFound().build();
        List<RichiestaSupportoRispostaDTO> risposta = richieste.stream()
                .map(RichiestaSupportoRispostaDTO::fromRichiestaSupporto)
                .toList();
        return ResponseEntity.ok(risposta);
    }


    /**
     * {@code POST /mentori/richieste/rispondi}
     * Invia una risposta testuale a una richiesta di supporto.
     * Il mentore autenticato non può rispondere a richieste già risolte.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param body il body della richiesta contenente {@code idRichiesta} (Long) e {@code risposta} (String)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se la risposta è stata inviata con successo;
     *         {@code 400 Bad Request} se i dati mancano, la richiesta è già risolta o la risposta non è valida;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 404 Not Found} se il mentore o la richiesta non esistono
     */
    @PostMapping("/richieste/rispondi")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> rispostaRichiestaSupporto(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMentore = account.getId();
        if(body.get("idRichiesta")==null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Long idRichiesta = ((Number) body.get("idRichiesta")).longValue();
        String risposta = (String) body.get("risposta");
        // Sicurezza: verifica che l'utente loggato sia il mentore che sta rispondendo
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi rispondere solo con il tuo account");
        }
        if (membroDelloStaffService.getMembroStaffById(idMentore) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        }
        if (risposta == null) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        RichiestaSupporto richiesta = richiestaSupportoService.getRichiestaSupporto(idRichiesta);
        if (richiesta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Richiesta non trovata");
        }
        if(richiesta.getHackathon().getMentori().stream().noneMatch(m -> m.getId().equals(idMentore)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: non sei assegnato a questo hackathon");
        if (richiestaSupportoService.isRichiestaSupportoRisolta(richiesta)) {
            return ResponseEntity.badRequest().body("Richiesta già risolta");
        }
        if (!richiestaSupportoService.rispostaRichiestaSupporto(richiesta, risposta)) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        return ResponseEntity.ok("Risposta inviata con successo");
    }

    //TODO controllo con segnalazioniController
    /**
     * {@code POST /mentori/segnalazioni/invia}
     * Segnala un team per violazione del regolamento durante un hackathon.
     * Il mentore autenticato deve essere assegnato all'hackathon specificato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param body il body della richiesta contenente {@code idTeam} (Long),
     *             {@code idHackathon} (Long) e {@code motivazione} (String)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 201 Created} se la segnalazione è stata inviata con successo;
     *         {@code 400 Bad Request} se i dati sono mancanti, vuoti o la segnalazione fallisce;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 404 Not Found} se il mentore non esiste
     */
    @PostMapping("/segnalazioni/invia")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> segnalaTeam(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMentore = auth.getName() == null ? null : account.getId();
        Long idTeam = ((Number) body.get("idTeam")).longValue();
        Long idHackathon = ((Number) body.get("idHackathon")).longValue();
        String motivazione = (String) body.get("motivazione");
        // Sicurezza: verifica che l'utente loggato sia il mentore che sta segnalando
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi segnalare solo con il tuo account");
        }
        if (membroDelloStaffService.getMembroStaffById(idMentore) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        }
        if (motivazione == null || motivazione.isBlank()) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if(hackathon.getMentori().stream().noneMatch(m -> m.getId().equals(idMentore)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: non sei assegnato a questo hackathon");
        try {
            if (!segnalazioneService.segnalaTeam(idTeam, idHackathon, idMentore, motivazione)) {
                return ResponseEntity.badRequest().body("Segnalazione non riuscita");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Segnalazione inviata con successo");
    }
}
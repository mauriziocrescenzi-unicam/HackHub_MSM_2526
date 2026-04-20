package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
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

@RestController
@RequestMapping("/mentori")
public class MentoreController {

    private final MentoreService mentoreService;
    private final HackathonService hackathonService;
    private final SegnalazioneService segnalazioneService;
    private final AccountService accountService;

    public MentoreController(MentoreService mentoreService,
                             HackathonService hackathonService,
                             SegnalazioneService segnalazioneService,
                             AccountService accountService  ) {
        this.mentoreService = mentoreService;
        this.hackathonService = hackathonService;
        this.segnalazioneService = segnalazioneService;
        this.accountService = accountService;
    }

    /**
     * POST /mentori/hackathons/lista
     * Restituisce la lista degli hackathon assegnati al mentore loggato, filtrati per stato.
     * Body: { "idMentore": 1, "stato": "IN_CORSO" }
     */
    @PostMapping("/hackathons/lista")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long idMentore = ((Number) body.get("idMentore")).longValue();
        String statoStr = (String) body.get("stato");
        // Sicurezza: verifica che l'utente loggato stia accedendo ai propri dati
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (statoStr == null) return ResponseEntity.badRequest().body(null);
        StatoHackathon stato = StatoHackathon.fromString(statoStr);
        List<Hackathon> lista = mentoreService.getListaHackathons(stato, idMentore);
        if (lista.isEmpty()) return ResponseEntity.notFound().build();
        List<HackathonRispostaDTO> risposta = lista.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * POST /mentori/richieste/lista
     * Restituisce le richieste di supporto per un hackathon specifico.
     * Body: { "idMentore": 1, "idHackathon": 10 }
     */
    @PostMapping("/richieste/lista")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<RichiestaSupportoRispostaDTO>> getRichiesteSupporto(@RequestBody Map<String, Long> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long idMentore = body.get("idMentore");
        Long idHackathon = body.get("idHackathon");
        // Sicurezza: verifica che l'utente loggato sia il mentore che sta richiedendo
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Verifica che il mentore esista
        if (mentoreService.getMentoreById(idMentore) == null) {
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
        List<RichiestaSupporto> richieste = mentoreService.getRichiesteSupporto(hackathon);
        if (richieste.isEmpty()) return ResponseEntity.notFound().build();
        List<RichiestaSupportoRispostaDTO> risposta = richieste.stream()
                .map(RichiestaSupportoRispostaDTO::fromRichiestaSupporto)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * POST /mentori/richieste/rispondi
     * Invia una risposta a una richiesta di supporto.
     * Body: { "idMentore": 1, "idRichiesta": 5, "risposta": "Testo della risposta..." }
     */
    @PostMapping("/richieste/rispondi")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> rispostaRichiestaSupporto(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMentore = ((Number) body.get("idMentore")).longValue();
        Long idRichiesta = ((Number) body.get("idRichiesta")).longValue();
        String risposta = (String) body.get("risposta");
        // Sicurezza: verifica che l'utente loggato sia il mentore che sta rispondendo
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi rispondere solo con il tuo account");
        }
        if (mentoreService.getMentoreById(idMentore) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        }
        if (idRichiesta == null || risposta == null) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        RichiestaSupporto richiesta = mentoreService.getRichiestaSupporto(idRichiesta);
        if (richiesta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Richiesta non trovata");
        }
        if (mentoreService.isRichiestaSupportoRisolta(richiesta)) {
            return ResponseEntity.badRequest().body("Richiesta già risolta");
        }
        if (!mentoreService.rispostaRichiestaSupporto(richiesta, risposta)) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
        return ResponseEntity.ok("Risposta inviata con successo");
    }

    /**
     * POST /mentori/segnalazioni/invia
     * Segnala un team per violazione del regolamento.
     * Body: { "idMentore": 1, "idTeam": 5, "idHackathon": 10, "motivazione": "Testo..." }
     */
    @PostMapping("/segnalazioni/invia")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> segnalaTeam(@RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMentore = ((Number) body.get("idMentore")).longValue();
        Long idTeam = ((Number) body.get("idTeam")).longValue();
        Long idHackathon = ((Number) body.get("idHackathon")).longValue();
        String motivazione = (String) body.get("motivazione");
        // Sicurezza: verifica che l'utente loggato sia il mentore che sta segnalando
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi segnalare solo con il tuo account");
        }
        if (mentoreService.getMentoreById(idMentore) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        }
        if (idTeam == null || idHackathon == null || motivazione == null || motivazione.isBlank()) {
            return ResponseEntity.badRequest().body("Dati non validi");
        }
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
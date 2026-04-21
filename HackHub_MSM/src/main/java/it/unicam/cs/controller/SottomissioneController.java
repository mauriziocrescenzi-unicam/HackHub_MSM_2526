package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SottomissioneCreazioneDTO;
import it.unicam.cs.dto.SottomissioneRispostaDTO;
import it.unicam.cs.model.Account;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Sottomissione;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SottomissioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sottomissioni")
public class SottomissioneController {
    private final SottomissioneService sottomissioneService;
    private final AccountService accountService;
    private final HackathonService hackathonService;

    public SottomissioneController(SottomissioneService sottomissioneService, AccountService accountService,  HackathonService hackathonService) {
        this.sottomissioneService = sottomissioneService;
        this.accountService = accountService;
        this.hackathonService = hackathonService;
    }
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean creato = sottomissioneService.inviaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione inviata con successo");
    }
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> aggiornaSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean agg = sottomissioneService.aggiornaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!agg) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione aggiornata con successo");
    }

    /**
     * Restituisce la lista degli hackathon assegnati all'utente loggato (se è giudice/staff) filtrati per stato.
     * GET /sottomissioni/hackathons?stato=IN_CORSO
     */
    @GetMapping("/hackathons")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<HackathonRispostaDTO>> getMieiHackathonDaGiudicare(@RequestParam String stato, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        StatoHackathon statoHackathon = StatoHackathon.fromString(stato);
        List<Hackathon> lista = sottomissioneService.getListaHackathonPerGiudice(account.getId(), statoHackathon);

        List<HackathonRispostaDTO> risposta = lista.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();

        if (risposta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(risposta);
    }

    /**
     * Restituisce le sottomissioni di un hackathon.
     * Verifica che l'utente loggato sia il giudice di quell'hackathon.
     * GET /sottomissioni/hackathons/{idHackathon}
     */
    @GetMapping("/hackathons/{idHackathon}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SottomissioneRispostaDTO>> getSottomissioni(@PathVariable long idHackathon, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        // Controllo di sicurezza: l'utente loggato deve essere il giudice di questo hackathon
        if (hackathon.getGiudice() == null || !hackathon.getGiudice().getId().equals(account.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<Sottomissione> sottomissioni = sottomissioneService.getSottomissioni(hackathon);
        if (sottomissioni.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<SottomissioneRispostaDTO> risposta = sottomissioni.stream()
                .map(SottomissioneRispostaDTO::fromSottomissione)
                .toList();
        return ResponseEntity.ok(risposta);
    }
    @GetMapping("/sottomissioni/{idSottomissione}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<SottomissioneRispostaDTO> getSottomissione( @PathVariable long idSottomissione) {
        Sottomissione sottomissione = sottomissioneService.getSottomissioneById(idSottomissione);
        if (sottomissione == null) {
            // Può significare: sottomissione non trovata OPPURE non autorizzato
            // Per sicurezza REST, restituiamo 404 in entrambi i casi
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        SottomissioneRispostaDTO risposta = SottomissioneRispostaDTO.fromSottomissione(sottomissione);
        return ResponseEntity.ok(risposta);
    }

    /**
     * Valuta una sottomissione.
     * PUT /sottomissioni/{idSottomissione}/valuta
     */
    @PutMapping("/{idSottomissione}/valuta")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> valutaSottomissione(@PathVariable long idSottomissione, @RequestBody Map<String, Object> body, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // Validazione input
        if (body.get("voto") == null || body.get("giudizio") == null) {
            return ResponseEntity.badRequest().body("Parametri voto e giudizio richiesti");
        }
        int voto;
        try {
            voto = Integer.parseInt(body.get("voto").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Voto non valido");
        }
        String giudizio = body.get("giudizio").toString();
        if (voto < 0 || voto > 10) return ResponseEntity.badRequest().body("Il voto deve essere compreso tra 0 e 10");
        if (giudizio == null || giudizio.isBlank()) return ResponseEntity.badRequest().body("Il giudizio non può essere vuoto");
        // Recupero entità
        Sottomissione sottomissione = sottomissioneService.getSottomissioneById(idSottomissione);
        if (sottomissione == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sottomissione non trovata");
        Hackathon hackathon = hackathonService.getHackathonByID(sottomissione.getIdHackathon());
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // Controllo di sicurezza: l'utente loggato deve essere il giudice di questo hackathon
        if (hackathon.getGiudice() == null || !hackathon.getGiudice().getId().equals(account.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: non sei il giudice di questo hackathon");
        }
        if (sottomissioneService.isSottomissioneValutata(sottomissione)) {
            return ResponseEntity.badRequest().body("Sottomissione già valutata");
        }
        boolean valutato = sottomissioneService.valutaSottomissione(sottomissione, voto, giudizio);
        if (!valutato) return ResponseEntity.badRequest().body("Valutazione non riuscita");
        return ResponseEntity.ok("Sottomissione valutata con successo");
    }
}

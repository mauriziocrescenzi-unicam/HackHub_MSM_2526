package it.unicam.cs.controller;

import it.unicam.cs.dto.ClassificaTeamDTO;
import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SottomissioneCreazioneDTO;
import it.unicam.cs.dto.SottomissioneRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SottomissioneService;
import it.unicam.cs.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sottomissioni")
public class SottomissioneController {
    private final SottomissioneService sottomissioneService;
    private final AccountService accountService;
    private final HackathonService hackathonService;
    private final TeamService teamService;

    public SottomissioneController(SottomissioneService sottomissioneService, AccountService accountService, HackathonService hackathonService, TeamService teamService) {
        this.sottomissioneService = sottomissioneService;
        this.accountService = accountService;
        this.hackathonService = hackathonService;
        this.teamService = teamService;
    }
    @PostMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> createSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean creato = sottomissioneService.inviaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione inviata con successo");
    }
    @PutMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> aggiornaSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean agg = sottomissioneService.aggiornaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!agg) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(200).body("Sottomissione aggiornata con successo");
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
        try {
            StatoHackathon.fromString(stato);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
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
     * GET /sottomissioni/hackathons/{idHackathon}
     * Restituisce le sottomissioni di un hackathon specifico.
     * Verifica che l'utente loggato sia il giudice di quell'hackathon.
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
        if (sottomissioni.isEmpty()) return ResponseEntity.notFound().build();
        List<SottomissioneRispostaDTO> risposta = sottomissioni.stream()
                .map(SottomissioneRispostaDTO::fromSottomissione)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * GET /sottomissioni/{idSottomissione}
     * Restituisce il dettaglio di una sottomissione specifica.
     * Verifica che l'utente loggato sia il giudice dell'hackathon a cui appartiene.
     */
    @GetMapping("/{idSottomissione}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<SottomissioneRispostaDTO> getSottomissione(@PathVariable long idSottomissione, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Sottomissione sottomissione = sottomissioneService.getSottomissioneById(idSottomissione);
        if (sottomissione == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // Controllo di sicurezza
        Hackathon hackathon = hackathonService.getHackathonByID(sottomissione.getIdHackathon());
        if (hackathon == null || hackathon.getGiudice() == null ||
                !hackathon.getGiudice().getId().equals(account.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(SottomissioneRispostaDTO.fromSottomissione(sottomissione));
    }

    /**
     * PUT /sottomissioni/{idSottomissione}/valuta
     * Valuta una sottomissione assegnando voto e giudizio.
     * L'ID viene passato nel path, voto e giudizio nel body.
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
        // Controllo di sicurezza
        if (hackathon == null || hackathon.getGiudice() == null ||
                !hackathon.getGiudice().getId().equals(account.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: non sei il giudice di questo hackathon");
        }
        if (sottomissioneService.isSottomissioneValutata(sottomissione)) {
            return ResponseEntity.badRequest().body("Sottomissione già valutata");
        }
        boolean valutato = sottomissioneService.valutaSottomissione(sottomissione, voto, giudizio);
        if (!valutato) return ResponseEntity.badRequest().body("Valutazione non riuscita");
        return ResponseEntity.ok("Sottomissione valutata con successo");
    }
    @GetMapping("/hackathons/{idHackathon}/classifica")
    @PreAuthorize("hasAnyRole('STAFF','USER')")
    public ResponseEntity<List<ClassificaTeamDTO>> getClassifica(@PathVariable long idHackathon) {
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        List<ClassificaTeamDTO> classifica = sottomissioneService.getClassifica(hackathon);
        if (classifica.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(classifica);
    }

    @PutMapping("/hackathons/{idHackathon}/vincitore")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> proclamaVincitore(@PathVariable long idHackathon,
                                                    @RequestBody Map<String, Long> body,
                                                    Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");

        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        if (!hackathon.getGiudice().getId().equals(account.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");

        Long idTeam = body.get("idTeam");
        if (idTeam == null) return ResponseEntity.badRequest().body("idTeam mancante");

        Team team = teamService.getTeamById(idTeam);
        if (team == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team non trovato");

        if (!sottomissioneService.proclamaTeamVincitore(hackathon, team))
            return ResponseEntity.badRequest().body("Proclamazione non riuscita: verifica che tutte le sottomissioni siano valutate e che l'hackathon sia in stato IN_VALUTAZIONE");

        return ResponseEntity.ok("Vincitore proclamato con successo");
    }
}

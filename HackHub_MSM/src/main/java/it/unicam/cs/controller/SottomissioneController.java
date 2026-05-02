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

/**
 * Controller REST per la gestione delle sottomissioni dei team agli hackathon.
 * Espone endpoint per inviare, aggiornare, recuperare e valutare le sottomissioni.
 * Gli endpoint di invio e aggiornamento richiedono il ruolo {@code UTENTE};
 * quelli di recupero, valutazione richiedono il ruolo, visualizzazione della classifica e proclamazione vincitore {@code STAFF}.
 */
@RestController
@RequestMapping("/sottomissioni")
public class SottomissioneController {
    private final SottomissioneService sottomissioneService;
    private final AccountService accountService;
    private final HackathonService hackathonService;
    private final TeamService teamService;
    /**
     * Costruisce un'istanza di {@code SottomissioneController} con le dipendenze necessarie.
     *
     * @param sottomissioneService service per la gestione delle sottomissioni
     * @param accountService       service per la gestione degli account
     * @param hackathonService     service per la gestione degli hackathon
     */
    public SottomissioneController(SottomissioneService sottomissioneService, AccountService accountService, HackathonService hackathonService, TeamService teamService) {
        this.sottomissioneService = sottomissioneService;
        this.accountService = accountService;
        this.hackathonService = hackathonService;
        this.teamService = teamService;
    }
    /**
     * {@code POST /sottomissioni}
     * Invia una nuova sottomissione per il team a cui appartiene l'utente autenticato.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param sottomissioneData i dati della sottomissione contenenti nome, link e ID dell'hackathon
     * @param auth              il contesto di autenticazione corrente
     * @return {@code 201 Created} se la sottomissione è stata inviata con successo;
     *         {@code 400 Bad Request} se i dati non sono validi o i requisiti non sono soddisfatti
     */
    @PostMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> createSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean creato = sottomissioneService.inviaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione inviata con successo");
    }
    /**
     * {@code PUT /sottomissioni}
     * Aggiorna la sottomissione esistente del team a cui appartiene l'utente autenticato.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param sottomissioneData i nuovi dati della sottomissione contenenti nome, link e ID dell'hackathon
     * @param auth              il contesto di autenticazione corrente
     * @return {@code 200 OK} se la sottomissione è stata aggiornata con successo;
     *         {@code 400 Bad Request} se i dati non sono validi o non esiste una sottomissione da aggiornare
     */
    @PutMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> aggiornaSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean agg = sottomissioneService.aggiornaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!agg) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(200).body("Sottomissione aggiornata con successo");
    }

    /**
     * {@code GET /sottomissioni/hackathons?stato=...}
     * Restituisce la lista degli hackathon assegnati al giudice autenticato, filtrati per stato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param stato il filtro sullo stato dell'hackathon (es. {@code "IN_VALUTAZIONE"})
     * @param auth  il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista degli hackathon filtrati;
     *         {@code 400 Bad Request} se il valore di {@code stato} non è valido;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 404 Not Found} se non ci sono hackathon corrispondenti
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
     * {@code GET /sottomissioni/hackathons/{idHackathon}}
     * Restituisce le sottomissioni di un hackathon specifico.
     * Verifica che l'utente autenticato sia il giudice dell'hackathon indicato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param idHackathon l'ID dell'hackathon di cui recuperare le sottomissioni
     * @param auth        il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista delle sottomissioni;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è il giudice dell'hackathon;
     *         {@code 404 Not Found} se l'hackathon non esiste o non ci sono sottomissioni
     */
    @GetMapping("/hackathons/{idHackathon}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SottomissioneRispostaDTO>> getSottomissioni(@PathVariable long idHackathon, Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
     * {@code GET /sottomissioni/{idSottomissione}}
     * Restituisce il dettaglio di una sottomissione specifica.
     * Verifica che l'utente autenticato sia il giudice dell'hackathon a cui appartiene la sottomissione.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param idSottomissione l'ID della sottomissione da recuperare
     * @param auth            il contesto di autenticazione corrente
     * @return {@code 200 OK} con i dettagli della sottomissione;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è il giudice dell'hackathon associato;
     *         {@code 404 Not Found} se la sottomissione non viene trovata
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
     * {@code PUT /sottomissioni/{idSottomissione}/valuta}
     * Valuta una sottomissione assegnando un voto (0-10) e un giudizio scritto.
     * Verifica che l'utente autenticato sia il giudice dell'hackathon associato
     * e che la sottomissione non sia già stata valutata.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param idSottomissione l'ID della sottomissione da valutare
     * @param body            il body della richiesta contenente {@code voto} (int) e {@code giudizio} (String)
     * @param auth            il contesto di autenticazione corrente
     * @return {@code 200 OK} se la valutazione è avvenuta con successo;
     *         {@code 400 Bad Request} se voto o giudizio non sono validi, o la sottomissione è già valutata;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è il giudice dell'hackathon;
     *         {@code 404 Not Found} se la sottomissione non viene trovata
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
    /**
     * {@code GET /sottomissioni/hackathons/{idHackathon}/classifica}
     * Restituisce la classifica dei team per un hackathon specifico, ordinata per punteggio decrescente.
     *
     * @param idHackathon l'ID dell'hackathon di cui calcolare la classifica
     * @return {@code 200 OK} con la lista ordinata di {@link ClassificaTeamDTO};
     *         {@code 404 Not Found} se l'hackathon non esiste o non ci sono sottomissioni valutate
     */
    @GetMapping("/hackathons/{idHackathon}/classifica")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<ClassificaTeamDTO>> getClassifica(@PathVariable long idHackathon) {
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        List<ClassificaTeamDTO> classifica = sottomissioneService.getClassifica(hackathon);
        if (classifica.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(classifica);
    }
    /**
     * {@code PUT /sottomissioni/hackathons/{idHackathon}/vincitore}
     * Proclama il team vincitore di un hackathon.
     *
     * @param idHackathon l'ID dell'hackathon per cui proclamare il vincitore
     * @param body        il body della richiesta contenente {@code idTeam} (Long)
     * @param auth        il contesto di autenticazione corrente
     * @return {@code 200 OK} se il vincitore è stato proclamato con successo;
     *         {@code 400 Bad Request} se {@code idTeam} è assente, non tutte le sottomissioni
     *         sono valutate o l'hackathon non è nello stato corretto;
     *         {@code 401 Unauthorized} se l'account non è trovato;
     *         {@code 403 Forbidden} se l'utente non è il giudice dell'hackathon;
     *         {@code 404 Not Found} se l'hackathon o il team non vengono trovati
     */
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

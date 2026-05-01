package it.unicam.cs.controller;

import it.unicam.cs.dto.SegnalazioneRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SegnalazioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * Controller REST per la gestione delle segnalazioni dei team durante gli hackathon.
 * Espone endpoint per inviare, accettare, rifiutare e recuperare le segnalazioni.
 * Accessibile solo agli utenti con ruolo {@code STAFF}.
 */
@RestController
@RequestMapping("/segnalazioni")
public class SegnalazioneController {
    private final SegnalazioneService segnalazioneService;
    private final AccountService accountService;
    private final HackathonService hackathonService;
    /**
     * Costruisce un'istanza di {@code SegnalazioneController} con le dipendenze necessarie.
     *
     * @param segnalazioneService service per la gestione delle segnalazioni
     * @param accountService      service per la gestione degli account
     * @param hackathonService    service per la gestione degli hackathon
     */
    public SegnalazioneController(SegnalazioneService segnalazioneService, AccountService accountService,HackathonService hackathonService) {
        this.segnalazioneService = segnalazioneService;
        this.accountService = accountService;
        this.hackathonService = hackathonService;
    }
    /**
     * {@code POST /segnalazioni}
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
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> segnalaTeam(@RequestBody Map<String, Object> body, Authentication auth){
        Account account = accountService.find(auth.getName());
        if (account == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        Long idMentore = auth.getName() == null ? null : account.getId();
        long idTeam = ((Number) body.get("idTeam")).longValue();
        long idHackathon = ((Number) body.get("idHackathon")).longValue();
        String motivazione = (String) body.get("motivazione");
        // Sicurezza: verifica che l'utente loggato sia il mentore che sta segnalando
        if (!account.getId().equals(idMentore)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato: puoi segnalare solo con il tuo account");
        }
        if (accountService.findById(idMentore) == null) {
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

    /**
     * {@code PUT /segnalazioni/{id}/accetta}
     * Accetta una segnalazione, rimuovendo il team dall'hackathon se ancora iscritto.
     * Solo l'organizzatore dell'hackathon relativo alla segnalazione può effettuare questa operazione.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param id   l'ID della segnalazione da accettare
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se la segnalazione è stata accettata e il team rimosso con successo;
     *         {@code 400 Bad Request} se l'utente non è l'organizzatore o il team non è più iscritto;
     *         {@code 404 Not Found} se la segnalazione non viene trovata
     */
    @PutMapping("/{id}/accetta")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> accettaSegnalazione (@PathVariable long id, Authentication auth){
        Segnalazione segnalazione= segnalazioneService.getSegnalazioneById(id);
        if(segnalazione==null) return ResponseEntity.notFound().build();
        Account account= accountService.find(auth.getName());
        if(!segnalazione.getHackathon().getOrganizzatore().equals(account)) return ResponseEntity.badRequest().body("Il tuo ruolo non consente di effettuare questa operazione");
        if(segnalazioneService.accettaSegnalazione(segnalazione))
            return ResponseEntity.ok("Segnalazione accettata con successo");
        return ResponseEntity.badRequest().body("Team non piu presente nel hackathon");
    }

    /**
     * {@code PUT /segnalazioni/{id}/rifiuta}
     * Rifiuta una segnalazione, aggiornandone lo stato senza rimuovere il team dall'hackathon.
     * Solo l'organizzatore dell'hackathon relativo alla segnalazione può effettuare questa operazione.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param id   l'ID della segnalazione da rifiutare
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se la segnalazione è stata rifiutata con successo;
     *         {@code 400 Bad Request} se l'utente non è l'organizzatore o l'operazione fallisce;
     *         {@code 404 Not Found} se la segnalazione non viene trovata
     */
    @PutMapping("/{id}/rifiuta")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> rifiutaSegnalazione (@PathVariable long id,Authentication auth){
        Segnalazione segnalazione= segnalazioneService.getSegnalazioneById(id);
        if(segnalazione==null) return ResponseEntity.notFound().build();
        Account account= accountService.find(auth.getName());
        if(!segnalazione.getHackathon().getOrganizzatore().equals(account)) return ResponseEntity.badRequest().body("Il tuo ruolo non consente di effettuare questa operazione");
        if(segnalazioneService.rifiutaSegnalazione(segnalazione))
            return ResponseEntity.ok("Segnalazione rifiutata con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }
    /**
     * {@code GET /segnalazioni/hackathon}
     * Restituisce la lista delle segnalazioni filtrate per hackathon e stato.
     * Verifica che tutti gli hackathon indicati appartengano all'organizzatore autenticato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param hackathonIds lista degli identificatori degli hackathon da filtrare;
     *                     non può essere vuota
     * @param stato        lo stato delle segnalazioni da filtrare (es. {@code DA_GESTIRE});
     *                     deve corrispondere a un valore valido di {@link StatoSegnalazione}
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista delle segnalazioni trovate;
     *         {@code 400 Bad Request} se i dati non sono validi, lo stato non è riconosciuto,
     *         o alcuni hackathon non appartengono all'organizzatore;
     *         {@code 404 Not Found} se non ci sono segnalazioni corrispondenti
     */
    @GetMapping("/hackathon")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SegnalazioneRispostaDTO>> getSegnalazioni( @RequestParam List<Long> hackathonIds,
                                                                          @RequestParam String stato, Authentication auth){
        Account organizzatore= accountService.find(auth.getName());
        if (stato==null) return ResponseEntity.badRequest().body(null);
        //controllo che i dati siano validi
        List<Long> ids = hackathonIds == null ? List.of() :
                ((List<?>) hackathonIds).stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        StatoSegnalazione statoSegnalazione= StatoSegnalazione.fromString(stato);
        if(statoSegnalazione==null) return ResponseEntity.badRequest().body(null);
        if(ids.isEmpty()) return ResponseEntity.badRequest().body(null);
        //trasformo i dati in hackathon
        List<Hackathon> hackathons = ids.stream()
                .map(hackathonService::getHackathonByID)
                .filter(Objects::nonNull) // scarta id non trovati
                .collect(Collectors.toList());
        if(!hackathons.stream().filter(hackathon -> !hackathon.getOrganizzatore().getId().equals(organizzatore.getId())).toList().isEmpty()) return ResponseEntity.badRequest().body(null);
        List<Segnalazione> list=segnalazioneService.getSegnalazioni(organizzatore,hackathons,statoSegnalazione);
        if(list.isEmpty()) return ResponseEntity.notFound().build();
        List<SegnalazioneRispostaDTO> risposta= list.stream().map(SegnalazioneRispostaDTO::fromSegnalazione).toList();
        return ResponseEntity.ok(risposta);
    }
}

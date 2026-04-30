package it.unicam.cs.controller;

import it.unicam.cs.dto.SegnalazioneRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SegnalazioneService;
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
     * Permette a un mentore di segnalare un team per violazione del regolamento.
     * Verifica che il mentore autenticato sia effettivamente assegnato all'hackathon indicato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param body il body della richiesta contenente {@code teamId} (Long), {@code hackathonId} (Long)
     *             e {@code motivazione} (String)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se la segnalazione è avvenuta con successo;
     *         {@code 400 Bad Request} se i dati non sono validi, l'hackathon o il mentore non vengono trovati,
     *         o il mentore non è assegnato all'hackathon
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> segnalaTeam(@RequestBody Map<String, Object> body, Authentication auth){
        if(body.get("teamId")==null || body.get("hackathonId")==null
                ||body.get("motivazione")==null) return ResponseEntity.badRequest().body("Dati non validi");
        long teamId = Long.parseLong(body.get("teamId").toString());
        long hackathonId = Long.parseLong(body.get("hackathonId").toString());
        Hackathon hackathon = hackathonService.getHackathonByID(hackathonId);
        Account mentore = accountService.find(auth.getName());
        if(hackathon==null || mentore==null) return ResponseEntity.badRequest().body("Hackathon o mentore non trovati");
        if(!hackathon.getMentori().contains(mentore)) return ResponseEntity.badRequest().body("Il tuo ruolo non consente di effettuare questa operazione");
        String motivazione = body.get("motivazione").toString();
        if(segnalazioneService.segnalaTeam(teamId,hackathonId,mentore.getId(),motivazione))
            return ResponseEntity.ok("Segnalazione effettuata con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
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
    //TODO cambiare in get
    /**
     * {@code GET /segnalazioni/hackathon}
     * Restituisce la lista delle segnalazioni filtrate per hackathon e stato.
     * Verifica che tutti gli hackathon indicati appartengano all'organizzatore autenticato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param body il body della richiesta contenente {@code hackathonIds} (List&lt;Long&gt;)
     *             e {@code stato} (String)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista delle segnalazioni trovate;
     *         {@code 400 Bad Request} se i dati non sono validi, lo stato non è riconosciuto,
     *         o alcuni hackathon non appartengono all'organizzatore;
     *         {@code 404 Not Found} se non ci sono segnalazioni corrispondenti
     */
    @GetMapping("/hackathon")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SegnalazioneRispostaDTO>> getSegnalazioni(@RequestBody Map<String, Object> body, Authentication auth){
        Account organizzatore= accountService.find(auth.getName());
        if (body.get("stato")==null) return ResponseEntity.badRequest().body(null);
        //controllo che i dati siano validi
        List<Long> ids = body.get("hackathonIds") == null ? List.of() :
                ((List<?>) body.get("hackathonIds")).stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        StatoSegnalazione statoSegnalazione= StatoSegnalazione.fromString(body.get("stato").toString());
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

package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonCreazioneDTO;
import it.unicam.cs.dto.HackathonInfoPubblicoDTO;
import it.unicam.cs.dto.HackathonModificaDTO;
import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
/**
 * Controller REST per la gestione degli hackathon.
 * Espone endpoint per la creazione, modifica, recupero e gestione dei mentori degli hackathon.
 */
@RestController
@RequestMapping("/hackathons")
public class HackathonController {

    private final HackathonService hackathonService;
    private final MentoreService mentoreService;
    private final AccountService accountService;
    /**
     * Costruisce un'istanza di {@code HackathonController} con le dipendenze necessarie.
     *
     * @param hackathonService  service per la gestione degli hackathon
     * @param mentoreService    service per la gestione dei mentori
     * @param accountService    service per la gestione degli account
     */
    public HackathonController(HackathonService hackathonService, MentoreService mentoreService, AccountService accountService) {
        this.hackathonService = hackathonService;
        this.mentoreService = mentoreService;
        this.accountService = accountService;
    }
    /**
     * {@code POST /hackathons}
     * Crea un nuovo hackathon. L'organizzatore viene ricavato dall'account autenticato.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param createDTO i dati di creazione dell'hackathon
     * @param auth      il contesto di autenticazione corrente
     * @return {@code 201 Created} se l'hackathon è stato creato con successo;
     *         {@code 400 Bad Request} se i dati non sono validi
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> createHackathon(@RequestBody HackathonCreazioneDTO createDTO, Authentication auth){
        Long organizzatoreId = accountService.findId(auth.getName());
        boolean creato = hackathonService.creaHackathon(
                createDTO.nome(),
                createDTO.regolamento(),
                createDTO.scadenzaIscrizione(),
                createDTO.dataInizio(),
                createDTO.dataFine(),
                createDTO.luogo(),
                createDTO.premioInDenaro(),
                createDTO.dimensioneMassimoTeam(),
                createDTO.stato(),
                organizzatoreId,
                createDTO.giudiceId(),
                createDTO.mentoriIds()
        );
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(HttpStatus.CREATED).body("Hackathon creato con successo");
    }
    /**
     * {@code PUT /hackathons/{id}}
     * Modifica i dati di un hackathon esistente.
     * Solo l'organizzatore dell'hackathon può effettuare la modifica.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param id           l'ID dell'hackathon da modificare
     * @param hackathonData i nuovi dati dell'hackathon
     * @param auth         il contesto di autenticazione corrente
     * @return {@code 200 OK} se la modifica è avvenuta con successo;
     *         {@code 404 Not Found} se l'hackathon non esiste;
     *         {@code 403 Forbidden} se l'utente non è l'organizzatore;
     *         {@code 400 Bad Request} se i dati non sono validi
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> modificaHackathon(@PathVariable long id,
                                                    @RequestBody HackathonModificaDTO hackathonData,
                                                    Authentication auth) {
        Account u = accountService.find(auth.getName());
        Hackathon hackathon = hackathonService.getHackathonByID(id);

        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        if (!hackathon.getOrganizzatore().getId().equals(u.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Il tuo ruolo non consente di effettuare questa operazione");
        if (!hackathonService.modificaHackathon(hackathon, hackathonData.nome(), hackathonData.regolamento(),
                hackathonData.scadenzaIscrizione(), hackathonData.dataInizio(), hackathonData.dataFine(),
                hackathonData.luogo(), hackathonData.premioInDenaro()))
            return ResponseEntity.badRequest().body("Dati non validi");

        return ResponseEntity.ok("Hackathon modificato con successo");
    }
    /**
     * {@code PUT /hackathons/{id}/mentori}
     * Aggiunge mentori a un hackathon esistente.
     * Solo l'organizzatore dell'hackathon può aggiungere mentori.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param id   l'ID dell'hackathon
     * @param body il body della richiesta contenente {@code mentoriIds} (lista di ID dei mentori)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se i mentori sono stati aggiunti con successo;
     *         {@code 404 Not Found} se l'hackathon non esiste;
     *         {@code 403 Forbidden} se l'utente non è l'organizzatore;
     *         {@code 400 Bad Request} se i dati non sono validi
     */
    @PutMapping("/{id}/mentori")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> aggiungereMentori(@PathVariable long id, @RequestBody Map<String, Object> body, Authentication auth){
        Account u = accountService.find(auth.getName());
        Hackathon hackathon = hackathonService.getHackathonByID(id);

        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        if (!hackathon.getOrganizzatore().getId().equals(u.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Il tuo ruolo non consente di effettuare questa operazione");

        List<?> raw = (List<?>) body.get("mentoriIds");
        if (raw == null) return ResponseEntity.badRequest().body("Dati non validi");
        List<Long> mentoriIds = raw.stream()
                .map(n -> ((Number) n).longValue())
                .toList();
        if(mentoreService.aggiungiMentori(mentoriIds,hackathon)) return ResponseEntity.ok("Hackathon modificato con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }
    /**
     * {@code GET /hackathons/{id}}
     * Restituisce i dettagli di un hackathon specifico.
     * Se l'utente non è autenticato, restituisce solo le informazioni pubbliche;
     * altrimenti restituisce la versione completa con tutti i dettagli.
     *
     * @param id   l'ID dell'hackathon da recuperare
     * @param auth il contesto di autenticazione corrente (può essere {@code null} o anonimo)
     * @return {@code 200 OK} con i dati dell'hackathon (pubblici o completi);
     *         {@code 404 Not Found} se l'hackathon non esiste
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getHackathon(@PathVariable long id,Authentication auth){
        Hackathon hackathon = hackathonService.getHackathonByID(id);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        if(auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.ok(HackathonInfoPubblicoDTO.fromHackathon(hackathon));
        }else {
            HackathonRispostaDTO dto= HackathonRispostaDTO.fromHackathon(hackathon);
            return ResponseEntity.ok(dto);
        }
    }

    /**
     * {@code GET /hackathons}
     * Restituisce la lista di tutti gli hackathon presenti nel sistema.
     *
     * @return {@code 200 OK} con la lista completa degli hackathon
     */
    @GetMapping
    public ResponseEntity<List<HackathonRispostaDTO>> getAllHackathon(){
        List<HackathonRispostaDTO> lista = hackathonService.getAllListaHackathon()
                .stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(lista);
    }


}

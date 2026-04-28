package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SottomissioneRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Sottomissione;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.MembroDelloStaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller REST per la gestione delle operazioni del Membro dello Staff.
 * Espone endpoint per visualizzare hackathon assegnati e sottomissioni.
 * Segue le best practices REST con GET per operazioni di lettura.
 */
@RestController
@RequestMapping("/staff")
public class MembroDelloStaffController {

    private final MembroDelloStaffService membroStaffService;
    private final AccountService accountService;

    public MembroDelloStaffController(MembroDelloStaffService membroStaffService, AccountService accountService) {
        this.membroStaffService = membroStaffService;
        this.accountService = accountService;
    }

    /*
         * PRIMA: Due metodi separati
     * ─────────────────────────
             * 1. GET /staff/hackathons
     *    - Nessun parametro
     *    - Restituiva TUTTI gli hackathon assegnati
     *
             * 2. PUT /staff (con body JSON)
     *    - Richiedeva {"stato": "..."} nel body
     *    - Restituiva hackathon filtrati per stato
     *    - Metodo HTTP non RESTful (PUT per lettura)
     *
             * DOPO: Un unico metodo GET con query parameter
     * ───────────────────────────────────────────
             * 1. Endpoint unificato: GET /staff/hackathons?stato=...
                *    - Più RESTful: GET per operazioni di lettura
     *    - Query parameter opzionale: facile da testare in Postman/cURL
     *    - Cache-friendly: le risposte GET possono essere cachate
     *
             * 2. Parametro opzionale (@RequestParam(required = false))
     *    - Se assente → restituisce tutti gli hackathon
     *    - Se presente → filtra per lo stato specificato
     *
             * 3. Gestione errori migliorata
     *    - Validazione esplicita dello stato con try-catch
                *    - Risposta 400 Bad Request per stati non validi
     *
             * 4. Codice più manutenibile
     *    - Meno duplicazione: logica di recupero in un solo punto
     *    - Più facile da estendere: aggiungere nuovi filtri è semplice
     *
             * COMPATIBILITÀ:
                * - Il comportamento funzionale è identico
     * - I client devono aggiornare le chiamate:
                *   PUT /staff {"stato":"IN_CORSO"} → GET /staff/hackathons?stato=IN_CORSO
 */
    @GetMapping("/hackathons")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(
            @RequestParam(required = false) String stato,
            Authentication auth) {
        Long id = accountService.findId(auth.getName());
        List<Hackathon> lista;
        if (stato == null || stato.isBlank()) {
            // Nessun filtro: recupera tutti
            lista = membroStaffService.getListaHackathon(id);
        } else {
            // Filtro per stato
            try {
                StatoHackathon statoHackathon = StatoHackathon.fromString(stato);
                lista = membroStaffService.getListaHackathons(statoHackathon, id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }
        if (lista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<HackathonRispostaDTO> risposta = lista.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }
}
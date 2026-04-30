package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.MembroDelloStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * Controller REST per la gestione delle operazioni del membro dello staff.
 * Espone endpoint per visualizzare gli hackathon assegnati.
 * Accessibile solo agli utenti con ruolo {@code STAFF}.
 */
@RestController
@RequestMapping("/staff")
public class MembroDelloStaffController {

    private final MembroDelloStaffService membroStaffService;
    private final AccountService accountService;
    /**
     * Costruisce un'istanza di {@code MembroDelloStaffController} con le dipendenze necessarie.
     *
     * @param membroStaffService service per la gestione dei membri dello staff
     * @param accountService     service per la gestione degli account
     */
    public MembroDelloStaffController(MembroDelloStaffService membroStaffService, AccountService accountService) {
        this.membroStaffService = membroStaffService;
        this.accountService = accountService;
    }

    /**
     * {@code GET /staff/hackathons}
     * Restituisce la lista degli hackathon associati al membro dello staff autenticato.
     * Se viene fornito il parametro {@code stato}, la lista viene filtrata di conseguenza;
     * altrimenti vengono restituiti tutti gli hackathon associati.
     * Richiede il ruolo {@code STAFF}.
     *
     * @param stato il filtro opzionale sullo stato dell'hackathon (es. {@code "IN_CORSO"})
     * @param auth  il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista degli hackathon;
     *         {@code 400 Bad Request} se il valore di {@code stato} non è valido;
     *         {@code 404 Not Found} se non ci sono hackathon associati
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
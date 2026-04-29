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
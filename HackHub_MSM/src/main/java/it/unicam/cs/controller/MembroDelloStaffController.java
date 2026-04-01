package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SottomissioneRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Sottomissione;
import it.unicam.cs.service.MembroDelloStaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per la gestione delle operazioni del Membro dello Staff.
 * Espone endpoint per visualizzare hackathon assegnati e sottomissioni.
 * Segue le best practices REST con GET per operazioni di lettura.
 */
@RestController
@RequestMapping("/staff")
public class MembroDelloStaffController {

    private final MembroDelloStaffService membroStaffService;

    public MembroDelloStaffController(MembroDelloStaffService membroStaffService) {
        this.membroStaffService = membroStaffService;
    }

    @GetMapping("/{id}/hackathons")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(@PathVariable long id) {
        List<Hackathon> hackathons = membroStaffService.getListaHackathon(id);
        if (hackathons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<HackathonRispostaDTO> risposta = hackathons.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/{idMembro}/hackathons/{idHackathon}/sottomissioni")
    public ResponseEntity<List<SottomissioneRispostaDTO>> getSottomissioni(@PathVariable long idMembro, @PathVariable long idHackathon) {
        List<Hackathon> hackathons = membroStaffService.getListaHackathon(idMembro);
        boolean assegnato = hackathons.stream()
                .anyMatch(h -> h.getId().equals(idHackathon));
        if (!assegnato) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Hackathon hackathon = hackathons.stream()
                .filter(h -> h.getId().equals(idHackathon))
                .findFirst()
                .orElse(null);
        if (hackathon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Sottomissione> sottomissioni = membroStaffService.getSottomissioni(hackathon);
        if (sottomissioni.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<SottomissioneRispostaDTO> risposta = sottomissioni.stream()
                .map(SottomissioneRispostaDTO::fromSottomissione)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/sottomissioni/{idSottomissione}")
    public ResponseEntity<SottomissioneRispostaDTO> getSottomissione(
            @PathVariable long idSottomissione) {
        Sottomissione sottomissione = membroStaffService.getSottomissione(idSottomissione);
        if (sottomissione == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        SottomissioneRispostaDTO risposta = SottomissioneRispostaDTO.fromSottomissione(sottomissione);
        return ResponseEntity.ok(risposta);
    }
}
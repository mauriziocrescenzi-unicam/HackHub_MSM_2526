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
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(Authentication auth) {
        Long id = accountService.findId(auth.getName());
        List<Hackathon> hackathons = membroStaffService.getListaHackathon(id);
        if (hackathons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<HackathonRispostaDTO> risposta = hackathons.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }
    @PutMapping()
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(@RequestBody Map<String, Object> body, Authentication auth){
        if (body.get("stato")==null) return ResponseEntity.badRequest().body(null);
        Long id = accountService.findId(auth.getName());
        StatoHackathon statoHackathon= StatoHackathon.fromString(body.get("stato").toString());
        List<Hackathon> list= membroStaffService.getListaHackathons(statoHackathon,id);
        List<HackathonRispostaDTO> risposta= list.stream().map(HackathonRispostaDTO::fromHackathon).toList();
        if(risposta.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(risposta);
    }


    @GetMapping("/hackathons/sottomissioni")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SottomissioneRispostaDTO>> getSottomissioni(@RequestBody Map<String, Object> body, Authentication auth) {
        Long idMembro = accountService.findId(auth.getName());
        if (body.get("idHackathon") == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Long idHackathon = ((Number) body.get("idHackathon")).longValue();
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
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<SottomissioneRispostaDTO> getSottomissione( @PathVariable long idSottomissione,Authentication auth) {
        Long idMembro = accountService.findId(auth.getName());
        Sottomissione sottomissione = membroStaffService.getSottomissione(idMembro, idSottomissione);
        if (sottomissione == null) {
            // Può significare: sottomissione non trovata OPPURE non autorizzato
            // Per sicurezza REST, restituiamo 404 in entrambi i casi
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        SottomissioneRispostaDTO risposta = SottomissioneRispostaDTO.fromSottomissione(sottomissione);
        return ResponseEntity.ok(risposta);
    }
}
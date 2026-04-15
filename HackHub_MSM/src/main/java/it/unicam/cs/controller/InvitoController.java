package it.unicam.cs.controller;

import it.unicam.cs.dto.InvitoRispostaDTO;
import it.unicam.cs.model.Account;
import it.unicam.cs.model.Invito;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.InvitoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inviti")
public class InvitoController {

    private final InvitoService invitoService;
    private final AccountService accountService;

    public InvitoController(InvitoService invitoService, AccountService accountService) {
        this.invitoService = invitoService;
        this.accountService = accountService;
    }

    // UC: Inviare un invito
    @PostMapping
    public ResponseEntity<String> inviareInvito(@RequestBody Map<String, Object> body, Authentication auth) {
        if (body.get("emailDestinatario") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Account mittente = accountService.find(auth.getName());

        if (mittente == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mittente non trovato");
            Account destinatario = accountService.find(body.get("emailDestinatario").toString());
        if (destinatario == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Destinatario non trovato");
        try {
            invitoService.inviareInvito(mittente, destinatario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Invito inviato con successo");
    }

    // UC: Visualizzare lista inviti
    @GetMapping("/{idUtente}")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<List<InvitoRispostaDTO>> getListaInviti(Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        List<Invito> inviti = invitoService.getInviti(account);
        if (inviti.isEmpty())
            return ResponseEntity.notFound().build();
        List<InvitoRispostaDTO> risposta = inviti.stream()
                .map(InvitoRispostaDTO::fromInvito)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    // UC: Valutare un invito
    @PutMapping("/valuta")
    public ResponseEntity<String> valutareInvito(@RequestBody Map<String, Object> body, Authentication auth) {
        if (body.get("idUtente") == null || body.get("idInvito") == null || body.get("risposta") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Long idUtente = ((Number) body.get("idUtente")).longValue();
        Long idInvito = ((Number) body.get("idInvito")).longValue();
        boolean risposta = (boolean) body.get("risposta");
        Account account = accountService.find(auth.getName());
        if (account == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utente non trovato");
        List<Invito> inviti = invitoService.getInviti(account);
        Invito invito = null;
        for (Invito i : inviti) {
            if (i.getId() == idInvito.longValue()) {
                invito = i;
                break;
            }
        }
        if (invito == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invito non trovato");
        try {
            invitoService.valutareInvito(invito, account, risposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Invito valutato con successo");
    }
}
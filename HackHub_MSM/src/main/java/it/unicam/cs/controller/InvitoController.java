package it.unicam.cs.controller;

import it.unicam.cs.dto.InvitoRispostaDTO;
import it.unicam.cs.model.Invito;
import it.unicam.cs.model.Utente;
import it.unicam.cs.service.InvitoService;
import it.unicam.cs.service.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inviti")
public class InvitoController {

    private final InvitoService invitoService;
    private final UtenteService utenteService;

    public InvitoController(InvitoService invitoService, UtenteService utenteService) {
        this.invitoService = invitoService;
        this.utenteService = utenteService;
    }

    @PostMapping
    public ResponseEntity<String> inviareInvito(@RequestBody Map<String, Object> body) {
        if (body.get("idMittente") == null || body.get("idDestinatario") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Long idMittente = ((Number) body.get("idMittente")).longValue();
        Long idDestinatario = ((Number) body.get("idDestinatario")).longValue();
        Utente mittente = utenteService.findById(idMittente);
        if (mittente == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mittente non trovato");
        Utente destinatario = utenteService.findById(idDestinatario);
        if (destinatario == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Destinatario non trovato");
        try {
            invitoService.inviareInvito(mittente, destinatario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Invito inviato con successo");
    }

    @GetMapping("/{idUtente}")
    public ResponseEntity<List<InvitoRispostaDTO>> getListaInviti(@PathVariable Long idUtente) {
        Utente utente = utenteService.findById(idUtente);
        if (utente == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        List<Invito> inviti = invitoService.getInviti(utente);
        if (inviti.isEmpty())
            return ResponseEntity.notFound().build();
        List<InvitoRispostaDTO> risposta = inviti.stream()
                .map(InvitoRispostaDTO::fromInvito)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @PutMapping("/{idInvito}")
    public ResponseEntity<String> valutareInvito(
             @PathVariable Long idInvito,
            @RequestBody Map<String, Object> body) {
        if (body.get("idUtente") == null || body.get("risposta") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Long idUtente = ((Number) body.get("idUtente")).longValue();
        boolean risposta = (boolean) body.get("risposta");
        Utente utente = utenteService.findById(idUtente);
        if (utente == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utente non trovato");
        List<Invito> inviti = invitoService.getInviti(utente);
        Invito invito = null;
        for (Invito i : inviti) {
            if (i.getId() == idInvito) {
                invito = i;
                break;
            }
        }
        if (invito == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invito non trovato");
        try {
            invitoService.valutareInvito(invito, utente, risposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Invito valutato con successo");
    }
}
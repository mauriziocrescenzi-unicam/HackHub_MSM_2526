package it.unicam.cs.controller;

import it.unicam.cs.dto.SottomissioneCreazioneDTO;
import it.unicam.cs.model.Account;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.SottomissioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sottomissioni")
public class SottomissioneController {
    private final SottomissioneService sottomissioneService;
    private final AccountService accountService;

    public SottomissioneController(SottomissioneService sottomissioneService, AccountService accountService) {
        this.sottomissioneService = sottomissioneService;
        this.accountService = accountService;
    }
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean creato = sottomissioneService.inviaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione inviata con successo");
    }
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> aggiornaSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData, Authentication auth){
        Account account = accountService.find(auth.getName());
        boolean agg = sottomissioneService.aggiornaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),account,sottomissioneData.idHackathon());
        if (!agg) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione aggiornata con successo");
    }
}

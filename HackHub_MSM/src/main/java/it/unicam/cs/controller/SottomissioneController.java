package it.unicam.cs.controller;

import it.unicam.cs.dto.SottomissioneCreazioneDTO;
import it.unicam.cs.service.SottomissioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sottomissioni")
public class SottomissioneController {
    private final SottomissioneService sottomissioneService;
    public SottomissioneController(SottomissioneService sottomissioneService) {
        this.sottomissioneService = sottomissioneService;
    }
    @PostMapping
    public ResponseEntity<String> createSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData){
        boolean creato = sottomissioneService.inviaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),sottomissioneData.idTeam(),sottomissioneData.idHackathon());
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione inviata con successo");
    }
    @PutMapping
    public ResponseEntity<String> aggiornaSottomissione(@RequestBody SottomissioneCreazioneDTO sottomissioneData){
        boolean agg = sottomissioneService.aggiornaSottomissione(sottomissioneData.nome(),sottomissioneData.link(),sottomissioneData.idTeam(),sottomissioneData.idHackathon());
        if (!agg) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(201).body("Sottomissione aggiornata con successo");
    }
}

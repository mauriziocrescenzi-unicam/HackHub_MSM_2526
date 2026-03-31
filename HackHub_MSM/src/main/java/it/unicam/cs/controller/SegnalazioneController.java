package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.SegnalazioneService;
import it.unicam.cs.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/segnalazioni")
public class SegnalazioneController {
    private final SegnalazioneService segnalazioneService;

    public SegnalazioneController(SegnalazioneService segnalazioneService) {
        this.segnalazioneService = segnalazioneService;
    }

    @PostMapping
    public ResponseEntity<String> segnalaTeam(@RequestBody Map<String, Object> body){
        if(body.get("teamId")==null || body.get("hackathonId")==null
                ||body.get("mentoreId")==null||body.get("motivazione")==null) return ResponseEntity.badRequest().body("Dati non validi");
        long teamId = Long.parseLong(body.get("teamId").toString());
        long hackathonId = Long.parseLong(body.get("hackathonId").toString());
        long mentoreId = Long.parseLong(body.get("mentoreId").toString());
        String motivazione = body.get("motivazione").toString();
        if(segnalazioneService.segnalaTeam(teamId,hackathonId,mentoreId,motivazione))
            return ResponseEntity.ok("Segnalazione effettuata con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }

    @PutMapping("/{id}/accetta")
    public ResponseEntity<String> accettaSegnalazione (@PathVariable long id){
        Segnalazione segnalazione= segnalazioneService.getSegnalazioneById(id);
        if(segnalazione==null) return ResponseEntity.notFound().build();
        if(segnalazioneService.accettaSegnalazione(segnalazione))
            return ResponseEntity.ok("Segnalazione accettata con successo");
        return ResponseEntity.badRequest().body("Team non piu presente nel hackathon");
    }


    @PutMapping("/{id}/rifiuta")
    public ResponseEntity<String> rifiutaSegnalazione (@PathVariable long id){
        Segnalazione segnalazione= segnalazioneService.getSegnalazioneById(id);
        if(segnalazione==null) return ResponseEntity.notFound().build();
        if(segnalazioneService.rifiutaSegnalazione(segnalazione))
            return ResponseEntity.ok("Segnalazione rifiutata con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }
}

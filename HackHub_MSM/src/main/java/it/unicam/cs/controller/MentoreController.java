package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.RichiestaSupportoRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mentori")
public class MentoreController {

    private final MentoreService mentoreService;
    private final HackathonService hackathonService;
    private final SegnalazioneService segnalazioneService;

    public MentoreController(MentoreService mentoreService,
                             HackathonService hackathonService,
                             SegnalazioneService segnalazioneService) {
        this.mentoreService = mentoreService;
        this.hackathonService = hackathonService;
        this.segnalazioneService = segnalazioneService;
    }

    // UC: Visualizzare lista hackathon del mentore
    @PutMapping("/{id}")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(
            @PathVariable long id,
            @RequestBody Map<String, Object> body) {
        if (body.get("stato") == null)
            return ResponseEntity.badRequest().body(null);
        StatoHackathon stato = StatoHackathon.fromString(body.get("stato").toString());
        List<Hackathon> lista = mentoreService.getListaHackathons(stato, id);
        if (lista.isEmpty())
            return ResponseEntity.notFound().build();
        List<HackathonRispostaDTO> risposta = lista.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    // UC: Visualizzare richieste di supporto
    @PutMapping("/{id}/richieste")
    public ResponseEntity<List<RichiestaSupportoRispostaDTO>> getRichiesteSupporto(
            @PathVariable long id,
            @RequestBody Map<String, Object> body) {
        if (body.get("idHackathon") == null)
            return ResponseEntity.badRequest().build();
        Long idHackathon = ((Number) body.get("idHackathon")).longValue();
        if (mentoreService.getMentoreById(id) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        boolean assegnato = hackathon.getMentori().stream()
                .anyMatch(m -> m.getId().equals(id));
        if (!assegnato)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<RichiestaSupporto> richieste = mentoreService.getRichiesteSupporto(hackathon);
        if (richieste.isEmpty())
            return ResponseEntity.notFound().build();
        List<RichiestaSupportoRispostaDTO> risposta = richieste.stream()
                .map(RichiestaSupportoRispostaDTO::fromRichiestaSupporto)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    // UC: Rispondere a una richiesta di supporto
    @PutMapping("/{id}/richieste/rispondi")
    public ResponseEntity<String> rispostaRichiestaSupporto(
            @PathVariable long id,
            @RequestBody Map<String, Object> body) {
        if (mentoreService.getMentoreById(id) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        if (body.get("idRichiesta") == null || body.get("risposta") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Long idRichiesta = ((Number) body.get("idRichiesta")).longValue();
        String risposta = body.get("risposta").toString();
        RichiestaSupporto richiesta = mentoreService.getRichiestaSupporto(idRichiesta);
        if (richiesta == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Richiesta non trovata");
        if (mentoreService.isRichiestaSupportoRisolta(richiesta))
            return ResponseEntity.badRequest().body("Richiesta già risolta");
        if (!mentoreService.rispostaRichiestaSupporto(richiesta, risposta))
            return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.ok("Risposta inviata con successo");
    }

    // UC: Segnalare un team
    @PostMapping("/{id}/segnalazioni")
    public ResponseEntity<String> segnalaTeam(
            @PathVariable long id,
            @RequestBody Map<String, Object> body) {
        if (mentoreService.getMentoreById(id) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        if (body.get("idTeam") == null || body.get("idHackathon") == null || body.get("motivazione") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        long idTeam = ((Number) body.get("idTeam")).longValue();
        long idHackathon = ((Number) body.get("idHackathon")).longValue();
        String motivazione = body.get("motivazione").toString();
        if (motivazione.isBlank())
            return ResponseEntity.badRequest().body("Motivazione non valida");
        try {
            if (!segnalazioneService.segnalaTeam(idTeam, idHackathon, id, motivazione))
                return ResponseEntity.badRequest().body("Segnalazione non riuscita");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Segnalazione inviata con successo");
    }
}
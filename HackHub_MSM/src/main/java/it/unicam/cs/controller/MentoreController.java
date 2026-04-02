package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.RichiestaSupportoRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.RichiestaSupporto;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.MentoreService;
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

    public MentoreController(MentoreService mentoreService, HackathonService hackathonService) {
        this.mentoreService = mentoreService;
        this.hackathonService = hackathonService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(
            @PathVariable long id,
            @RequestBody Map<String, Object> body) {
        if (body.get("stato") == null)
            return ResponseEntity.badRequest().body(null);
        StatoHackathon stato = StatoHackathon.fromString(body.get("stato").toString());
        if (stato == null)
            return ResponseEntity.badRequest().body(null);
        List<Hackathon> lista = mentoreService.getListaHackathons(stato, id);
        if (lista.isEmpty())
            return ResponseEntity.notFound().build();
        List<HackathonRispostaDTO> risposta = lista.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/{id}/hackathons/{idHackathon}/richieste")
    public ResponseEntity<List<RichiestaSupportoRispostaDTO>> getRichiesteSupporto(
            @PathVariable long id,
            @PathVariable long idHackathon) {
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

    @PutMapping("/{id}/richieste/{idRichiesta}")
    public ResponseEntity<String> rispostaRichiestaSupporto(
            @PathVariable long id,
            @PathVariable Long idRichiesta,
            @RequestBody Map<String, Object> body) {
        if (mentoreService.getMentoreById(id) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mentore non trovato");
        if (body.get("risposta") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
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
}
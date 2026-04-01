package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SottomissioneRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Sottomissione;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.service.GiudiceService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SottomissioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/giudici")
public class GiudiceController {
    private final GiudiceService giudiceService;
    private final SottomissioneService sottomissioneService;
    private final HackathonService hackathonService;

    public GiudiceController(GiudiceService giudiceService, SottomissioneService sottomissioneService, HackathonService hackathonService) {
        this.giudiceService = giudiceService;
        this.sottomissioneService = sottomissioneService;
        this.hackathonService = hackathonService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(@PathVariable long id, @RequestBody Map<String, Object> body) {
        if (body.get("stato") == null) {
            return ResponseEntity.badRequest().body(null);
        }
        StatoHackathon statoHackathon = StatoHackathon.fromString(body.get("stato").toString());
        List<Hackathon> lista = giudiceService.getListaHackathon(statoHackathon, id);
        List<HackathonRispostaDTO> risposta = lista.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        if (risposta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/{id}/hackathons/{idHackathon}/sottomissioni")
    public ResponseEntity<List<SottomissioneRispostaDTO>> getSottomissioni(@PathVariable long id, @PathVariable long idHackathon) {
        if (giudiceService.getGiudiceById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Hackathon hackathon = hackathonService.getHackathonByID(idHackathon);
        if (hackathon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (hackathon.getGiudice() == null ||
                !hackathon.getGiudice().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        List<Sottomissione> sottomissioni = giudiceService.getSottomissioni(hackathon);
        if (sottomissioni.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<SottomissioneRispostaDTO> risposta = sottomissioni.stream()
                .map(SottomissioneRispostaDTO::fromSottomissione)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/{idGiudice}/sottomissioni/{idSottomissione}/valutata")
    public ResponseEntity<Boolean> isSottomissioneValutata(@PathVariable long idGiudice, @PathVariable long idSottomissione) {
        Sottomissione sottomissione = sottomissioneService.getSottomissioneById(idSottomissione);
        if (sottomissione == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Hackathon hackathon = hackathonService.getHackathonByID(sottomissione.getIdHackathon());
        if (hackathon == null || hackathon.getGiudice() == null ||
                !hackathon.getGiudice().getId().equals(idGiudice)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean valutata = giudiceService.isSottomissioneValutata(sottomissione);
        return ResponseEntity.ok(valutata);
    }

    @PutMapping("/{idGiudice}/sottomissioni/{idSottomissione}/valuta")
    public ResponseEntity<String> valutaSottomissione(
            @PathVariable long idGiudice,
            @PathVariable long idSottomissione,
            @RequestBody Map<String, Object> body) {
        if (body.get("voto") == null || body.get("giudizio") == null) {
            return ResponseEntity.badRequest().body("Parametri voto e giudizio richiesti");
        }
        int voto;
        try {
            voto = Integer.parseInt(body.get("voto").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Voto non valido");
        }
        String giudizio = body.get("giudizio").toString();
        if (voto < 0 || voto > 10) {
            return ResponseEntity.badRequest().body("Il voto deve essere compreso tra 0 e 10");
        }
        if (giudizio == null || giudizio.isBlank()) {
            return ResponseEntity.badRequest().body("Il giudizio non può essere vuoto");
        }
        Sottomissione sottomissione = sottomissioneService.getSottomissioneById(idSottomissione);
        if (sottomissione == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sottomissione non trovata");
        }
        Hackathon hackathon = hackathonService.getHackathonByID(sottomissione.getIdHackathon());
        if (hackathon == null || hackathon.getGiudice() == null ||
                !hackathon.getGiudice().getId().equals(idGiudice)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Non autorizzato: giudice non assegnato a questo hackathon");
        }
        if (giudiceService.isSottomissioneValutata(sottomissione)) {
            return ResponseEntity.badRequest().body("Sottomissione già valutata");
        }
        boolean valutato = giudiceService.valutaSottomissione(sottomissione, voto, giudizio);
        if (!valutato) {
            return ResponseEntity.badRequest().body("Valutazione non riuscita");
        }
        return ResponseEntity.ok("Sottomissione valutata con successo");
    }
}

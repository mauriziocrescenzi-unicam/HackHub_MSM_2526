package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.service.GiudiceService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SottomissioneService;
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
}

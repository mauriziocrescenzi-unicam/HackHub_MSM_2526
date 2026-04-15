package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SegnalazioneRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.OrganizzatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organizzatori")
public class OrganizzatoreController {
    private final OrganizzatoreService organizzatoreService;
    private final HackathonService hackathonService;
    public OrganizzatoreController(OrganizzatoreService organizzatoreService, HackathonService hackathonService) {
        this.organizzatoreService = organizzatoreService;
        this.hackathonService = hackathonService;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<HackathonRispostaDTO>> getListaHackathon(@PathVariable long id, @RequestBody Map<String, Object> body){
        if (body.get("stato")==null) return ResponseEntity.badRequest().body(null);
        StatoHackathon statoHackathon= StatoHackathon.fromString(body.get("stato").toString());
        List<Hackathon> list= organizzatoreService.getListaHackathons(statoHackathon,id);
        List<HackathonRispostaDTO> risposta= list.stream().map(HackathonRispostaDTO::fromHackathon).toList();
        if(risposta.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(risposta);
    }

    @PutMapping("/{id}/segnalazioni")
    public ResponseEntity<List<SegnalazioneRispostaDTO>> getSegnalazioni(@PathVariable long id, @RequestBody Map<String, Object> body){
        Account organizzatore= organizzatoreService.getOrganizzatoreById(id);
        if (organizzatore==null)return ResponseEntity.notFound().build();
        if (body.get("stato")==null) return ResponseEntity.badRequest().body(null);
        //controllo che i dati siano validi
        List<Long> ids = body.get("hackathonIds") == null ? List.of() :
                ((List<?>) body.get("hackathonIds")).stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        StatoSegnalazione statoSegnalazione= StatoSegnalazione.fromString(body.get("stato").toString());
        if(statoSegnalazione==null) return ResponseEntity.badRequest().body(null);
        if(ids.isEmpty()) return ResponseEntity.badRequest().body(null);
        //trasformo i dati in hackathon
        List<Hackathon> hackathons = ids.stream()
                .map(hackathonService::getHackathonByID)
                .filter(Objects::nonNull) // scarta id non trovati
                .collect(Collectors.toList());
        List<Segnalazione> list=organizzatoreService.getSegnalazioni(organizzatore,hackathons,statoSegnalazione);
        if(list.isEmpty()) return ResponseEntity.notFound().build();
        List<SegnalazioneRispostaDTO> risposta= list.stream().map(SegnalazioneRispostaDTO::fromSegnalazione).toList();
        return ResponseEntity.ok(risposta);
    }

}

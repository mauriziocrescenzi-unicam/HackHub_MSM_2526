package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonCreazioneDTO;
import it.unicam.cs.service.HackathonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/hackathons")
public class HackathonController {

    private final HackathonService hackathonService;
    public HackathonController(HackathonService hackathonService) {this.hackathonService = hackathonService;}

    @PostMapping
    public ResponseEntity<String> createHackathon(@RequestBody HackathonCreazioneDTO createDTO){
        boolean creato = hackathonService.creaHackathon(
                createDTO.nome(),
                createDTO.regolamento(),
                createDTO.scadenzaIscrizione(),
                createDTO.dataInizio(),
                createDTO.dataFine(),
                createDTO.luogo(),
                createDTO.premioInDenaro(),
                createDTO.dimensioneMassimoTeam(),
                createDTO.stato(),
                createDTO.organizzatoreId(),
                createDTO.giudiceId(),
                createDTO.mentoriIds()
        );
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(HttpStatus.CREATED).body("Hackathon creato con successo");
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> modificaHackathon(@RequestBody Map<String, Object> hackathonData){
        long idHackathon = ((Number) hackathonData.get("id")).longValue();
        LocalDateTime scadenzaIscrizione= (LocalDateTime) hackathonData.get("scadenzaIscrizione");
        LocalDateTime dataInizio= (LocalDateTime) hackathonData.get("dataInizio");
        LocalDateTime dataFine= (LocalDateTime) hackathonData.get("dataFine");
        double premioInDenaro= (double) hackathonData.get("premioInDenaro");
        if(hackathonService.modificaHackathon(hackathonService.getHackathonByID(idHackathon),(String) hackathonData.get("nome"),
                (String) hackathonData.get("regolamento"),scadenzaIscrizione,dataInizio,dataFine,
                (String) hackathonData.get("luogo"),premioInDenaro))
            return ResponseEntity.status(HttpStatus.OK).body("Hackathon modificato con successo");
        return ResponseEntity.badRequest().body("Dati non validi");

    }

}

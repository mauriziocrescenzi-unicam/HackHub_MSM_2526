package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonCreazioneDTO;
import it.unicam.cs.dto.HackathonModificaDTO;
import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Team;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.MentoreService;
import it.unicam.cs.service.TeamHackathonService;
import it.unicam.cs.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hackathons")
public class HackathonController {

    private final HackathonService hackathonService;
    private final MentoreService mentoreService;
    private final TeamHackathonService teamHackathonService;
    private final TeamService teamService;

    public HackathonController(HackathonService hackathonService, MentoreService mentoreService, TeamHackathonService teamHackathonService, TeamService teamService) {
        this.hackathonService = hackathonService;
        this.mentoreService = mentoreService;
        this.teamHackathonService = teamHackathonService;
        this.teamService = teamService;
    }

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
    public ResponseEntity<String> modificaHackathon(@PathVariable long id, @RequestBody HackathonModificaDTO hackathonData){
        Hackathon hackathon = hackathonService.getHackathonByID(id);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        if (!hackathonService.modificaHackathon(hackathon, hackathonData.nome(), hackathonData.regolamento(),
                hackathonData.scadenzaIscrizione(), hackathonData.dataInizio(), hackathonData.dataFine(),
                hackathonData.luogo(), hackathonData.premioInDenaro()))
            return ResponseEntity.badRequest().body("Dati non validi");

        return ResponseEntity.ok("Hackathon modificato con successo");

    }
    @PutMapping("/{id}/mentori")
    public ResponseEntity<String> aggiungereMentori(@PathVariable long id, @RequestBody Map<String, Object> body){
        Hackathon hackathon = hackathonService.getHackathonByID(id);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        List<?> raw = (List<?>) body.get("mentoriIds");
        if (raw == null) return ResponseEntity.badRequest().body("Dati non validi");
        List<Long> mentoriIds = raw.stream()
                .map(n -> ((Number) n).longValue())
                .toList();
        if(mentoreService.aggiungiMentori(mentoriIds,hackathon)) return ResponseEntity.ok("Hackathon modificato con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }
    @GetMapping("/{id}")
    public ResponseEntity<HackathonRispostaDTO> getHackathon(@PathVariable long id){
        Hackathon hackathon = hackathonService.getHackathonByID(id);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        HackathonRispostaDTO dto= HackathonRispostaDTO.fromHackathon(hackathon);
        return ResponseEntity.ok(dto);
    }

    //TODO testing e modifica metodi alla base uno riceve 2 entita uno due long
    @PutMapping("{id}/iscriviTeam")
    public ResponseEntity<String> iscrivereTeam(@PathVariable long id,@RequestBody Map<String,Object> body){
        Hackathon hackathon = hackathonService.getHackathonByID(id);
        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");
        if(body.get("teamId")==null) return ResponseEntity.badRequest().body("Dati non validi");
        long teamId= ((Number) body.get("teamId")).longValue();
        Team team = teamService.getTeamById(teamId);
        if(teamHackathonService.iscrivereTeam(hackathon,team)) return ResponseEntity.ok("Team iscritto al hackathon");
        return ResponseEntity.badRequest().body("Dati non validi");
    }
    @PutMapping("/{id}/disiscrivereTeam")
    public ResponseEntity<String> disiscrivereTeam(@PathVariable long id,@RequestBody Map<String,Object> body){
        if (id <0)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");
        if(body.get("teamId")==null) return ResponseEntity.badRequest().body("Dati non validi");
        long teamId= ((Number) body.get("teamId")).longValue();
        if(teamHackathonService.disiscrivereTeam(teamId,id)) return ResponseEntity.ok("Team iscritto al hackathon");
        return ResponseEntity.badRequest().body("Dati non validi");
    }

}

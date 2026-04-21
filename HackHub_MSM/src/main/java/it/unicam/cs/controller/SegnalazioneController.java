package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.SegnalazioneRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.HackathonService;
import it.unicam.cs.service.SegnalazioneService;
import it.unicam.cs.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/segnalazioni")
public class SegnalazioneController {
    private final SegnalazioneService segnalazioneService;
    private final AccountService accountService;
    private final HackathonService hackathonService;

    public SegnalazioneController(SegnalazioneService segnalazioneService, AccountService accountService,HackathonService hackathonService) {
        this.segnalazioneService = segnalazioneService;
        this.accountService = accountService;
        this.hackathonService = hackathonService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> segnalaTeam(@RequestBody Map<String, Object> body, Authentication auth){
        if(body.get("teamId")==null || body.get("hackathonId")==null
                ||body.get("mentoreId")==null||body.get("motivazione")==null) return ResponseEntity.badRequest().body("Dati non validi");
        long teamId = Long.parseLong(body.get("teamId").toString());
        long hackathonId = Long.parseLong(body.get("hackathonId").toString());
        Hackathon hackathon = hackathonService.getHackathonByID(hackathonId);
        Account mentore = accountService.find(auth.getName());
        if(hackathon==null || mentore==null) return ResponseEntity.badRequest().body("Hackathon o mentore non trovati");
        if(!hackathon.getMentori().contains(mentore)) return ResponseEntity.badRequest().body("Il tuo ruolo non consente di effettuare questa operazione");
        String motivazione = body.get("motivazione").toString();
        if(segnalazioneService.segnalaTeam(teamId,hackathonId,mentore.getId(),motivazione))
            return ResponseEntity.ok("Segnalazione effettuata con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }


    @PutMapping("/{id}/accetta")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> accettaSegnalazione (@PathVariable long id, Authentication auth){
        Segnalazione segnalazione= segnalazioneService.getSegnalazioneById(id);
        if(segnalazione==null) return ResponseEntity.notFound().build();
        Account account= accountService.find(auth.getName());
        if(!segnalazione.getHackathon().getOrganizzatore().equals(account)) return ResponseEntity.badRequest().body("Il tuo ruolo non consente di effettuare questa operazione");
        if(segnalazioneService.accettaSegnalazione(segnalazione))
            return ResponseEntity.ok("Segnalazione accettata con successo");
        return ResponseEntity.badRequest().body("Team non piu presente nel hackathon");
    }


    @PutMapping("/{id}/rifiuta")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> rifiutaSegnalazione (@PathVariable long id,Authentication auth){
        Segnalazione segnalazione= segnalazioneService.getSegnalazioneById(id);
        if(segnalazione==null) return ResponseEntity.notFound().build();
        Account account= accountService.find(auth.getName());
        if(!segnalazione.getHackathon().getOrganizzatore().equals(account)) return ResponseEntity.badRequest().body("Il tuo ruolo non consente di effettuare questa operazione");
        if(segnalazioneService.rifiutaSegnalazione(segnalazione))
            return ResponseEntity.ok("Segnalazione rifiutata con successo");
        return ResponseEntity.badRequest().body("Dati non validi");
    }
    @GetMapping("/hackathon")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SegnalazioneRispostaDTO>> getSegnalazioni(@RequestBody Map<String, Object> body, Authentication auth){
        Account organizzatore= accountService.find(auth.getName());
        if (body.get("stato")==null) return ResponseEntity.badRequest().body(null);
        //controllo che i dati siano validi
        List<Long> ids = body.get("hackathonIds") == null ? List.of() :
                ((List<?>) body.get("hackathonIds")).stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        StatoSegnalazione statoSegnalazione= StatoSegnalazione.fromString(body.get("stato").toString());
        if(statoSegnalazione==null) return ResponseEntity.badRequest().body(null);
        if(ids.isEmpty()) return ResponseEntity.badRequest().body(null);
        //trasformo i dati in hackathon
        List<Hackathon> hackathons = ids.stream()
                .map(hackathonService::getHackathonByID)
                .filter(Objects::nonNull) // scarta id non trovati
                .collect(Collectors.toList());
        if(!hackathons.stream().filter(hackathon -> hackathon.getOrganizzatore()!=organizzatore).toList().isEmpty()) return ResponseEntity.badRequest().body(null);
        List<Segnalazione> list=segnalazioneService.getSegnalazioni(organizzatore,hackathons,statoSegnalazione);
        if(list.isEmpty()) return ResponseEntity.notFound().build();
        List<SegnalazioneRispostaDTO> risposta= list.stream().map(SegnalazioneRispostaDTO::fromSegnalazione).toList();
        return ResponseEntity.ok(risposta);
    }
}

package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonCreazioneDTO;
import it.unicam.cs.dto.HackathonInfoPubblicoDTO;
import it.unicam.cs.dto.HackathonModificaDTO;
import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.repository.AccountRepository;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hackathons")
public class HackathonController {

    private final HackathonService hackathonService;
    private final MentoreService mentoreService;
    private final MembroDelloStaffService membroStaffService;
    private final AccountService accountService;

    public HackathonController(HackathonService hackathonService, MentoreService mentoreService, MembroDelloStaffService membroStaffService, AccountService accountService) {
        this.hackathonService = hackathonService;
        this.mentoreService = mentoreService;
        this.membroStaffService = membroStaffService;
        this.accountService = accountService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> createHackathon(@RequestBody HackathonCreazioneDTO createDTO, Authentication auth){
        Long organizzatoreId = accountService.findId(auth.getName());
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
                organizzatoreId,
                createDTO.giudiceId(),
                createDTO.mentoriIds()
        );
        if (!creato) return ResponseEntity.badRequest().body("Dati non validi");
        return ResponseEntity.status(HttpStatus.CREATED).body("Hackathon creato con successo");
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> modificaHackathon(@PathVariable long id,
                                                    @RequestBody HackathonModificaDTO hackathonData,
                                                    Authentication auth) {
        Account u = accountService.find(auth.getName());
        Hackathon hackathon = hackathonService.getHackathonByID(id);

        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        if (!hackathon.getOrganizzatore().getId().equals(u.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Il tuo ruolo non consente di effettuare questa operazione");
        if (!hackathonService.modificaHackathon(hackathon, hackathonData.nome(), hackathonData.regolamento(),
                hackathonData.scadenzaIscrizione(), hackathonData.dataInizio(), hackathonData.dataFine(),
                hackathonData.luogo(), hackathonData.premioInDenaro()))
            return ResponseEntity.badRequest().body("Dati non validi");

        return ResponseEntity.ok("Hackathon modificato con successo");
    }
    @PutMapping("/{id}/mentori")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> aggiungereMentori(@PathVariable long id, @RequestBody Map<String, Object> body, Authentication auth){
        Account u = accountService.find(auth.getName());
        Hackathon hackathon = hackathonService.getHackathonByID(id);

        if (hackathon == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hackathon non trovato");

        if (!hackathon.getOrganizzatore().getId().equals(u.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Il tuo ruolo non consente di effettuare questa operazione");

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

    /**
     * Test recupero delle informazioni
     */
    @GetMapping
    public ResponseEntity<List<HackathonRispostaDTO>> getAllHackathon(){
        List<HackathonRispostaDTO> lista = hackathonService.getAllListaHackathon()
                .stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(lista);
    }

    /**
     * Test recupero delle informazioni pubbliche
     */
    @GetMapping("/{id}/informazioni-pubbliche")
    public ResponseEntity<HackathonInfoPubblicoDTO> getInformazioniPubbliche(@PathVariable long id){
        Hackathon h = hackathonService.getHackathonByID(id);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(HackathonInfoPubblicoDTO.fromHackathon(h));
    }

    /**
     * Test recupero delle informazioni
     */
    @GetMapping("/{id}/informazioni")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<HackathonRispostaDTO> getAllInformazioni(@PathVariable long id){
        Hackathon h = hackathonService.getHackathonByID(id);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(HackathonRispostaDTO.fromHackathon(h));
    }
}

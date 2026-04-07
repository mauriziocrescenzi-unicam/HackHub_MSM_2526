package it.unicam.cs.controller;

import it.unicam.cs.dto.HackathonRispostaDTO;
import it.unicam.cs.dto.MembroTeamRispostaDTO;
import it.unicam.cs.dto.SottomissioneCreazioneDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/membro-team")
public class MembroTeamController {

    private final MembroTeamService membroTeamService;
    private final TeamHackathonService teamHackathonService;
    private final HackathonService hackathonService;
    private final SottomissioneService sottomissioneService;
    private final TeamService teamService;

    public MembroTeamController(MembroTeamService membroTeamService,
                                TeamHackathonService teamHackathonService,
                                HackathonService hackathonService,
                                SottomissioneService sottomissioneService,
                                TeamService teamService) {
        this.membroTeamService = membroTeamService;
        this.teamHackathonService = teamHackathonService;
        this.hackathonService = hackathonService;
        this.sottomissioneService = sottomissioneService;
        this.teamService = teamService;
    }

    // ==================== ISCRIZIONE HACKATHON ====================

    /**
     * GET /membro-team/{idMembro}/hackathons/iscritto
     * Restituisce la lista degli hackathon a cui il team del membro è iscritto.
     * Usa DTO per non esporre entità JPA.
     */
    @GetMapping("/{idMembro}/hackathons/iscritto")
    public ResponseEntity<List<HackathonRispostaDTO>> isIscrittoHackathon(@PathVariable long idMembro) {
        Team team = teamService.getTeamByMembroId(idMembro);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        List<Hackathon> hackathons = teamHackathonService.isIscrittoHackathon(team);
        if (hackathons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<HackathonRispostaDTO> risposta = hackathons.stream()
                .map(HackathonRispostaDTO::fromHackathon)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * GET /membro-team/{idMembro}/hackathons/stato
     * Verifica se almeno un hackathon del team è in uno degli stati specificati.
     * Corrisponde a checkStato(listaHackathon, stati...) nel sequence diagram.
     *
     * @param idMembro ID del membro del team
     * @param body Corpo della richiesta con array di stati (es. {"stati": ["IN_ISCRIZIONE", "CONCLUSO"]})
     * @return true se almeno un hackathon è in uno degli stati, false altrimenti
     */
    @GetMapping("/{idMembro}/hackathons/stato")
    public ResponseEntity<Boolean> checkStato(@PathVariable long idMembro, @RequestBody Map<String, Object> body) {
        if (body.get("stati") == null) {
            return ResponseEntity.badRequest().body(null);
        }
        // Estrae gli stati dal body e li converte in enum
        List<?> statiRaw = (List<?>) body.get("stati");
        StatoHackathon[] stati = statiRaw.stream()
                .map(s -> StatoHackathon.fromString(s.toString()))
                .toArray(StatoHackathon[]::new);
        // Recupera il team del membro
        Team team = teamService.getTeamByMembroId(idMembro);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        // Recupera gli hackathon a cui il team è iscritto
        List<Hackathon> listaHackathon = teamHackathonService.isIscrittoHackathon(team);
        // Verifica lo stato usando HackathonService.checkStato (varargs)
        boolean risultato = hackathonService.checkStato(listaHackathon, stati);
        return ResponseEntity.ok(risultato);
    }

    // ==================== SOTTOMISSIONE ====================

    /**
     * GET /membro-team/{idMembro}/hackathons/{idHackathon}/sottomissione/presente
     * Verifica se esiste già una sottomissione per il team nell'hackathon.
     * Corrisponde a isPresente(idTeam, idHackathon) nel sequence diagram.
     *
     * @param idMembro ID del membro del team
     * @param idHackathon ID dell'hackathon
     * @return true se la sottomissione esiste, false altrimenti
     */
    @GetMapping("/{idMembro}/hackathons/{idHackathon}/sottomissione/presente")
    public ResponseEntity<Boolean> isPresente(@PathVariable long idMembro,
                                              @PathVariable long idHackathon) {

        Team team = teamService.getTeamByMembroId(idMembro);
        if (team == null) {
            return ResponseEntity.notFound().build();
        }
        boolean presente = sottomissioneService.isPresente(team.getId(), idHackathon);
        return ResponseEntity.ok(presente);
    }

   

    // ==================== GESTIONE MEMBRI TEAM ====================

    /**
     * GET /membro-team/team/{idTeam}/membri
     * Restituisce la lista dei membri di un team specifico.
     * Usa DTO per non esporre entità JPA.
     */
    @GetMapping("/team/{idTeam}/membri")
    public ResponseEntity<List<MembroTeamRispostaDTO>> getMembri(@PathVariable long idTeam) {
        List<MembroTeam> membri = membroTeamService.getMembri(idTeam);
        if (membri.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<MembroTeamRispostaDTO> risposta = membri.stream()
                .map(MembroTeamRispostaDTO::fromMembroTeam)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * PUT /membro-team/{idMembro}/team/{idTeam}/abbandona
     * Gestisce l'abbandono volontario di un membro da un team.
     * Corrisponde ad abbandonaTeam(idMembro, idTeam) nel sequence diagram.
     *
     * @param idMembro ID del membro che abbandona
     * @param idTeam ID del team da abbandonare
     * @return Messaggio di successo o errore
     */
    @PutMapping("/{idMembro}/team/{idTeam}/abbandona")
    public ResponseEntity<String> abbandonaTeam(@PathVariable long idMembro, @PathVariable long idTeam) {
        boolean abbandonato = membroTeamService.abbandonaTeam(idMembro, idTeam);
        if (!abbandonato) {
            return ResponseEntity.badRequest().body("Abbandono non riuscito");
        }
        return ResponseEntity.ok("Membro ha abbandonato il team con successo");
    }

    /**
     * DELETE /membro-team/{idMembro}/team/{idTeam}/elimina
     * Elimina un membro specifico da un team (rimozione forzata).
     * Corrisponde ad eliminaMembro(idMembro, idTeam) nel sequence diagram.
     *
     * @param idMembro ID del membro da eliminare
     * @param idTeam ID del team da cui rimuovere
     * @return Messaggio di successo o errore
     */
    @DeleteMapping("/{idMembro}/team/{idTeam}/elimina")
    public ResponseEntity<String> eliminaMembro(@PathVariable long idMembro, @PathVariable long idTeam) {
        boolean eliminato = membroTeamService.eliminaMembro(idMembro, idTeam);
        if (!eliminato) {
            return ResponseEntity.badRequest().body("Eliminazione non riuscita");
        }
        return ResponseEntity.ok("Membro eliminato dal team con successo");
    }

}

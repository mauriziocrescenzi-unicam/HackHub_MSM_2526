package it.unicam.cs.controller;

import it.unicam.cs.dto.InvitoRispostaDTO;
import it.unicam.cs.model.Account;
import it.unicam.cs.model.Invito;
import it.unicam.cs.service.AccountService;
import it.unicam.cs.service.InvitoService;
import it.unicam.cs.service.MembroTeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * Controller REST per la gestione degli inviti tra membri del team.
 * Espone endpoint per l'invio, la visualizzazione e la valutazione degli inviti.
 * Accessibile solo agli utenti con ruolo {@code UTENTE}.
 */
@RestController
@RequestMapping("/inviti")
public class InvitoController {

    private final InvitoService invitoService;
    private final AccountService accountService;
    private final MembroTeamService membroTeamService;
    /**
     * Costruisce un'istanza di {@code InvitoController} con le dipendenze necessarie.
     *
     * @param invitoService     service per la gestione degli inviti
     * @param accountService    service per la gestione degli account
     * @param membroTeamService service per la gestione dei membri del team
     */
    public InvitoController(InvitoService invitoService, AccountService accountService, MembroTeamService membroTeamService) {
        this.invitoService = invitoService;
        this.accountService = accountService;
        this.membroTeamService = membroTeamService;
    }

    /**
     * {@code POST /inviti}
     * Invia un invito a un altro utente per unirsi al team del mittente.
     * Il mittente deve essere membro di un team.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param body il body della richiesta contenente {@code emailDestinatario}
     * @param auth il contesto di autenticazione corrente
     * @return {@code 201 Created} se l'invito è stato inviato con successo;
     *         {@code 400 Bad Request} se i dati non sono validi o l'invio fallisce;
     *         {@code 403 Forbidden} se il mittente non è membro di un team;
     *         {@code 404 Not Found} se mittente o destinatario non vengono trovati
     */
    @PostMapping
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> inviareInvito(@RequestBody Map<String, Object> body, Authentication auth) {
        if (body.get("emailDestinatario") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        Account mittente = accountService.find(auth.getName());

        if (mittente == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mittente non trovato");
        if(membroTeamService.getMembroById(mittente.getId())==null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non puoi inviare un invito");
        Account destinatario = accountService.find(body.get("emailDestinatario").toString());
        if (destinatario == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Destinatario non trovato");
        try {
            invitoService.inviareInvito(mittente, destinatario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Invito inviato con successo");
    }

    /**
     * {@code GET /inviti}
     * Restituisce la lista degli inviti in attesa per l'utente autenticato.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} con la lista degli inviti pendenti (vuota se non ce ne sono);
     *         {@code 404 Not Found} se l'account non viene trovato
     */
    @GetMapping()
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<List<InvitoRispostaDTO>> getListaInviti(Authentication auth) {
        Account account = accountService.find(auth.getName());
        if (account == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        List<Invito> inviti = invitoService.getInviti(account);
        if (inviti.isEmpty())
            return ResponseEntity.ok(List.of());
        List<InvitoRispostaDTO> risposta = inviti.stream()
                .map(InvitoRispostaDTO::fromInvito)
                .toList();
        return ResponseEntity.ok(risposta);
    }

    /**
     * {@code PUT /inviti/valuta}
     * Permette all'utente autenticato di accettare o rifiutare un invito ricevuto.
     * Richiede il ruolo {@code UTENTE}.
     *
     * @param body il body della richiesta contenente {@code idInvito} (Long) e {@code risposta} (boolean)
     * @param auth il contesto di autenticazione corrente
     * @return {@code 200 OK} se l'invito è stato valutato con successo;
     *         {@code 400 Bad Request} se i dati non sono validi o la valutazione fallisce;
     *         {@code 404 Not Found} se l'utente o l'invito non vengono trovati
     */
    @PutMapping("/valuta")
    @PreAuthorize("hasRole('UTENTE')")
    public ResponseEntity<String> valutareInvito(@RequestBody Map<String, Object> body, Authentication auth) {
        if (body.get("idInvito") == null || body.get("risposta") == null)
            return ResponseEntity.badRequest().body("Dati non validi");
        long idInvito = ((Number) body.get("idInvito")).longValue();
        boolean risposta = (boolean) body.get("risposta");
        Account account = accountService.find(auth.getName());
        if (account == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utente non trovato");
        List<Invito> inviti = invitoService.getInviti(account);
        Invito invito = null;
        for (Invito i : inviti) {
            if (i.getId() == idInvito) {
                invito = i;
                break;
            }
        }
        if (invito == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invito non trovato");
        try {
            invitoService.valutareInvito(invito, account, risposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Invito valutato con successo");
    }
}
package it.unicam.cs.controller;

import it.unicam.cs.dto.AccountResponse;
import it.unicam.cs.dto.LoginRequest;
import it.unicam.cs.dto.RegisterRequest;
import it.unicam.cs.model.Account;
import it.unicam.cs.repository.AccountRepository;
import it.unicam.cs.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import it.unicam.cs.model.Ruolo;
/**
 * Controller REST per la gestione dell'autenticazione degli utenti.
 * Espone endpoint per la registrazione, il login e il recupero dei dati dell'account autenticato.
 */
@RestController
@RequestMapping("/auth")
public class AutenticazioneController {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    /**
     * Costruisce un'istanza di {@code AutenticazioneController} con le dipendenze necessarie.
     *
     * @param authenticationManager gestore dell'autenticazione Spring Security
     * @param accountRepository     repository per l'accesso agli account
     * @param passwordEncoder       encoder per la cifratura delle password
     * @param jwtUtil               utility per la generazione dei token JWT
     */
    public AutenticazioneController(AuthenticationManager authenticationManager,
                                    AccountRepository accountRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.accountRepository     = accountRepository;
        this.passwordEncoder       = passwordEncoder;
        this.jwtUtil               = jwtUtil;
    }

    /**
     * {@code POST /auth/registrazione}
     * Registra un nuovo account con il ruolo specificato nella richiesta.
     * Se la registrazione va a buon fine, esegue il login automatico e restituisce il token JWT.
     *
     * @param req la richiesta di registrazione contenente email, password, ruolo, nome e cognome
     * @return il token JWT se la registrazione è riuscita;
     *         {@code 409 Conflict} se l'email è già registrata;
     *         {@code 500 Internal Server Error} in caso di errore imprevisto
     */
    @PostMapping("/registrazione")
    public ResponseEntity<?> registrazione(@RequestBody RegisterRequest req) {
        if (accountRepository.existsByEmail(req.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email già registrata");
        }
        try {
            Account account = new Account(
                    req.email(),
                    passwordEncoder.encode(req.password()),
                    Ruolo.valueOf(req.ruolo()),
                    req.nome(),
                    req.cognome()
            );
            accountRepository.save(account);

            // Login automatico dopo registrazione
            return accesso(new LoginRequest(req.email(), req.password()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione");
        }


    }

    /**
     * {@code POST /auth/accesso}
     * Autentica l'utente con email e password e restituisce un token JWT.
     *
     * @param req la richiesta di login contenente email e password
     * @return il token JWT se le credenziali sono valide;
     *         {@code 401 Unauthorized} se le credenziali non sono corrette
     */
    @PostMapping("/accesso")
    public ResponseEntity<?> accesso(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );

            // Ricava il ruolo dall'account nel DB
            Account account = accountRepository.findByEmail(req.email())
                    .orElseThrow();

            String token = jwtUtil.generateToken(
                    account.getEmail(),
                    account.getRuolo().name()
            );

            return ResponseEntity.ok(token);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenziali non valide");
        }
    }

    /**
     * {@code GET /auth/me}
     * Restituisce i dati dell'account attualmente autenticato.
     *
     * @param auth il contesto di autenticazione corrente, contenente l'email dell'utente
     * @return un {@link AccountResponse} con id, email, nome, cognome e ruolo dell'utente autenticato;
     *         lancia {@link IllegalArgumentException} se l'account non viene trovato
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Account non trovato"));

        return ResponseEntity.ok(new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getNome(),
                account.getCognome(),
                account.getRuolo().name()
        ));
    }

}
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

@RestController
@RequestMapping("/auth")
public class AutenticazioneController {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
     * POST /auth/signup
     * Registra un nuovo account con ruolo USER di default.
     */
    @PostMapping("/registrazione")
    public ResponseEntity<?> registrazione(@RequestBody RegisterRequest req) {
        if (accountRepository.existsByEmail(req.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email già registrata");
        }

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
    }

    /**
     * POST /auth/login
     * Autentica e restituisce il JWT.
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
     * GET /auth/me
     * Restituisce i dati dell'account loggato.
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
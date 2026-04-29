package it.unicam.cs.service;

import it.unicam.cs.model.Account;
import it.unicam.cs.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Implementazione personalizzata di {@link UserDetailsService} per Spring Security.
 * Carica i dettagli dell'utente a partire dall'email, utilizzandola come username.
 */
@Service
public class CustomAccountDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;
    /**
     * Carica i dettagli dell'utente in base all'email fornita.
     * Il ruolo dell'account viene mappato come authority Spring Security
     * nel formato {@code ROLE_<NOME_RUOLO>}.
     *
     * @param email l'email dell'account da autenticare
     * @return un oggetto {@link UserDetails} contenente email, password e ruolo dell'utente
     * @throws UsernameNotFoundException se nessun account è associato all'email fornita
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Account non trovato con email: " + email));

        // Passa il ruolo a Spring Security come authority
        return new User(
                account.getEmail(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRuolo().name()))
        );
    }
}
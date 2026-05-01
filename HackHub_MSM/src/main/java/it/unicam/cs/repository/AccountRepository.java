package it.unicam.cs.repository;

import it.unicam.cs.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository per la gestione della persistenza degli account utente.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base
 * più le query personalizzate definite di seguito.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Restituisce l'account associato all'email specificata.
     *
     * @param email l'email da cercare
     * @return un {@link Optional} contenente l'account trovato,
     *         o vuoto se nessun account corrisponde all'email
     */
    Optional<Account> findByEmail(String email);
    /**
     * Verifica se esiste un account con l'email specificata.
     *
     * @param email l'email da verificare
     * @return {@code true} se esiste almeno un account con quella email, {@code false} altrimenti
     */
    boolean existsByEmail(String email);
}

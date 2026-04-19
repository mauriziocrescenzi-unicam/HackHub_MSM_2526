package it.unicam.cs.repository;

import it.unicam.cs.model.Account;
import it.unicam.cs.model.Ruolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Account> findAllByRuolo(Ruolo ruolo);

    List<Account> findByRuolo(Ruolo ruolo);
}

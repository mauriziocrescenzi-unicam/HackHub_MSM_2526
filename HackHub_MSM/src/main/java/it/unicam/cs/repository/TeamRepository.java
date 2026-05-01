package it.unicam.cs.repository;

import it.unicam.cs.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository per la gestione della persistenza dei team.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base sui team.
 */
public interface TeamRepository extends JpaRepository<Team,Long> {
}

package it.unicam.cs.repository;

import it.unicam.cs.model.Invito;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository per la gestione della persistenza degli inviti.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base sugli inviti.
 */
public interface InvitoRepository extends JpaRepository<Invito,Long> {
}

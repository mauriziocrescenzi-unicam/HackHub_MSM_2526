package it.unicam.cs.repository;

import it.unicam.cs.model.Sottomissione;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository per la gestione della persistenza delle sottomissioni.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base sulle sottomissioni.
 */
public interface SottomissioneRepository extends JpaRepository<Sottomissione,Long> {
}

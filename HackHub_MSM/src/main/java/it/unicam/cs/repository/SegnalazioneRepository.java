package it.unicam.cs.repository;

import it.unicam.cs.model.Segnalazione;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository per la gestione della persistenza delle segnalazioni.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base sulle segnalazioni.
 */
public interface SegnalazioneRepository extends JpaRepository<Segnalazione,Long> {
}

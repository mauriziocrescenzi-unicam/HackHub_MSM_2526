package it.unicam.cs.repository;

import it.unicam.cs.model.Segnalazione;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegnalazioneRepository extends JpaRepository<Segnalazione,Long> {
}

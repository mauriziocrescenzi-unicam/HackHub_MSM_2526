package it.unicam.cs.repository;

import it.unicam.cs.model.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HackathonRepository extends JpaRepository<Hackathon,Long> {
    // Trova hackathon per organizzatore
    List<Hackathon> findByOrganizzatoreId(Long organizzatoreId);
    // Trova hackathon per giudice
    List<Hackathon> findByGiudiceId(Long giudiceId);
    // Trova hackathon per mentore (relazione ManyToMany)
    List<Hackathon> findByMentoriId(Long mentoreId);
}

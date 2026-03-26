package it.unicam.cs.repository;

import it.unicam.cs.model.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon,Long> {
}

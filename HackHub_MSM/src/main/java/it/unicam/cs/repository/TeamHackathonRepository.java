package it.unicam.cs.repository;

import it.unicam.cs.model.TeamHackathon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Repository per la gestione della persistenza delle associazioni team-hackathon.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base
 * più le query personalizzate per cercare le iscrizioni per team o per coppia team-hackathon.
 */
public interface TeamHackathonRepository extends JpaRepository<TeamHackathon,Long> {
    /**
     * Restituisce l'associazione tra il team e l'hackathon specificati.
     *
     * @param idTeam      l'ID del team
     * @param idHackathon l'ID dell'hackathon
     * @return il {@link TeamHackathon} corrispondente, oppure {@code null} se non trovato
     */
    TeamHackathon findByTeamIdAndHackathonId(Long idTeam, Long idHackathon);
    /**
     * Restituisce tutte le associazioni hackathon relative al team specificato.
     *
     * @param idTeam l'ID del team
     * @return lista delle {@link TeamHackathon} associate al team
     */
    List<TeamHackathon> findByTeamId(Long idTeam);

}

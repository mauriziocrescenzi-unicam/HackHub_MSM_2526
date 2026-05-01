package it.unicam.cs.repository;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.RichiestaSupporto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Repository per la gestione della persistenza delle richieste di supporto.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base
 * più le query personalizzate per filtrare per hackathon.
 */
public interface RichiestaSupportoRepository extends JpaRepository<RichiestaSupporto, Long> {

    /**
     * Restituisce tutte le richieste di supporto relative a un hackathon.
     *
     * @param hackathon Hackathon di riferimento
     * @return Lista di richieste di supporto
     */
    List<RichiestaSupporto> findByHackathon(Hackathon hackathon);

}
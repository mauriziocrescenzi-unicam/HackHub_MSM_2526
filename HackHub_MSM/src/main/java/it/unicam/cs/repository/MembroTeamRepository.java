package it.unicam.cs.repository;

import it.unicam.cs.model.MembroTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
/**
 * Repository per la gestione della persistenza delle associazioni membro-team.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base
 * più le query personalizzate per recuperare i membri in base all'account o al team.
 */
public interface MembroTeamRepository extends JpaRepository<MembroTeam,Long> {
    /**
     * Restituisce il membro del team identificato dalla coppia account-team.
     *
     * @param accountId l'ID dell'account
     * @param teamId    l'ID del team
     * @return un {@link Optional} contenente il {@link MembroTeam} trovato,
     *         o vuoto se non esiste tale associazione
     */
    Optional<MembroTeam> findByAccountIdAndTeamId(Long accountId, Long teamId);
    /**
     * Restituisce tutti i membri appartenenti al team specificato.
     *
     * @param teamId l'ID del team
     * @return lista dei {@link MembroTeam} del team indicato
     */
    List<MembroTeam> findByTeamId(Long teamId);
    /**
     * Restituisce il membro del team associato all'account specificato.
     *
     * @param accountId l'ID dell'account
     * @return un {@link Optional} contenente il {@link MembroTeam} trovato,
     *         o vuoto se l'account non è membro di nessun team
     */
    Optional<MembroTeam> findByAccountId(Long accountId);
}

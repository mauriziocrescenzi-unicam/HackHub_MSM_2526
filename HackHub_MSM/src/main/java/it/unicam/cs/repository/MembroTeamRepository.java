package it.unicam.cs.repository;

import it.unicam.cs.model.MembroTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembroTeamRepository extends JpaRepository<MembroTeam,Long> {
    MembroTeam findByUtenteId(Long utenteId);

    Optional<MembroTeam> findByAccountIdAndTeamId(Long accountId, Long teamId);

    List<MembroTeam> findByTeamId(Long teamId);

    Optional<MembroTeam> findByAccountId(Long accountId);
}

package it.unicam.cs.repository;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.StatoHackathon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Repository per la gestione della persistenza degli hackathon.
 * Estende {@link JpaRepository} fornendo le operazioni CRUD di base
 * più le query personalizzate per filtrare per ruolo dello staff e stato.
 */
public interface HackathonRepository extends JpaRepository<Hackathon,Long> {
    /**
     * Restituisce tutti gli hackathon organizzati dall'account specificato.
     *
     * @param organizzatoreId l'ID dell'account organizzatore
     * @return lista degli hackathon di cui l'account è organizzatore
     */
    List<Hackathon> findByOrganizzatoreId(Long organizzatoreId);
    /**
     * Restituisce tutti gli hackathon in cui l'account specificato è il giudice.
     *
     * @param giudiceId l'ID dell'account giudice
     * @return lista degli hackathon di cui l'account è giudice
     */
    List<Hackathon> findByGiudiceId(Long giudiceId);
    /**
     * Restituisce tutti gli hackathon in cui l'account specificato è uno dei mentori.
     * Sfrutta la relazione {@code ManyToMany} tra hackathon e mentori.
     *
     * @param mentoreId l'ID dell'account mentore
     * @return lista degli hackathon a cui il mentore è assegnato
     */
    List<Hackathon> findByMentoriId(Long mentoreId);
    /**
     * Restituisce tutti gli hackathon che si trovano nello stato specificato.
     *
     * @param stato lo stato degli hackathon da cercare
     * @return lista degli hackathon con lo stato indicato
     */
    List<Hackathon> findByStato(StatoHackathon stato);
}

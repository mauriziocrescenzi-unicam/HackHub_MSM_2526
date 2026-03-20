package it.unicam.cs.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/**
 * Classe DAO generica per operazioni CRUD di base su entità JPA.
 * <p>
 * Fornisce un'implementazione standardizzata per la persistenza di entità
 * tramite JPA/Hibernate, gestendo automaticamente {@link EntityManager}
 * e transazioni. Utilizza il pattern Generic Type per essere riutilizzabile
 * con qualsiasi classe entità annotata con {@link jakarta.persistence.Entity}.
 * </p>
 * <p><strong>Nota:</strong> Ogni metodo apre e chiude un {@link EntityManager}
 * indipendente. Per operazioni batch complesse, considerare l'uso di un
 * {@link EntityManager} condiviso con gestione manuale della transazione.</p>
 */
public class StandardPersistence<T> {

    /**
     * Classe Java dell'entità gestita da questo DAO.
     */
    private final Class<T> entityClass;

    /**
     * Costruisce un'istanza di StandardPersistence per la classe entità specificata.
     *
     * @param entityClass la classe {@link Class} dell'entità da gestire
     */
    public StandardPersistence(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Persiste una nuova entità o aggiorna un'entità esistente nel database.
     * <p>
     * Utilizza {@link EntityManager#merge(Object)} per gestire sia insert che update.
     * La transazione viene gestita automaticamente con rollback in caso di eccezione.
     * </p>
     *
     * @param entity l'entità da persistere o aggiornare
     * @throws RuntimeException se si verifica un errore durante la transazione
     */
    public void create(T entity) {
        EntityManager em = JPAUtility.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Aggiorna un'entità esistente nel database e restituisce l'istanza gestita.
     * <p>
     * Utilizza {@link EntityManager#merge(Object)} per sincronizzare lo stato
     * dell'entità con il database. L'entità restituita è l'istanza gestita
     * dal persistence context e dovrebbe essere utilizzata per ulteriori operazioni.
     * </p>
     *
     * @param entity l'entità da aggiornare
     * @return l'entità aggiornata gestita dal persistence context
     * @throws RuntimeException se si verifica un errore durante la transazione
     */
    public T update(T entity) {
        EntityManager em = JPAUtility.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Rimuove un'entità dal database.
     * <p>
     * Se l'entità non è già gestita dal persistence context, viene prima
     * merged per ottenerne il riferimento gestito, quindi rimossa.
     * </p>
     *
     * @param entity l'entità da eliminare
     * @throws RuntimeException se si verifica un errore durante la transazione
     */
    public void delete(T entity) {
        EntityManager em = JPAUtility.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T managed = em.contains(entity) ? entity : em.merge(entity);
            em.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Recupera un'entità dal database tramite il suo identificatore primario.
     *
     * @param id l'identificatore primario (Long) dell'entità da cercare
     * @return l'entità trovata, o {@code null} se non esiste
     */
    public T findById(Long id) {
        EntityManager em = JPAUtility.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * Recupera tutte le entità di questo tipo dal database.
     *
     * @return una {@link List} contenente tutte le entità, o una lista vuota se nessuna è presente
     */
    public List<T> getAll() {
        EntityManager em = JPAUtility.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Verifica se esiste un'entità con il dato identificatore.
     *
     * @param id l'identificatore primario da verificare
     * @return {@code true} se l'entità esiste, {@code false} altrimenti
     */
    public boolean existsById(Long id) {
        return findById(id) != null;
    }

    /**
     * Conta il numero totale di entità di questo tipo nel database.
     *
     * @return il numero di entità presenti
     */
    public long count() {
        EntityManager em = JPAUtility.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class
            ).getSingleResult();
        } finally {
            em.close();
        }
    }
}
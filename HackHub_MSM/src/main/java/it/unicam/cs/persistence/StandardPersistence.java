package it.unicam.cs.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class StandardPersistence<T> {

    private final Class<T> entityClass;

    public StandardPersistence(Class<T> entityClass) {
        this.entityClass = entityClass;
    }


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


    public T findById(Long id) {
        EntityManager em = JPAUtility.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }


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

    public boolean existsById(Long id) {
        return findById(id) != null;
    }


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
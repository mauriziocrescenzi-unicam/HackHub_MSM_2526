package it.unicam.cs.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtility {

    private static EntityManagerFactory emf;

    private JPAUtility() {}

    private static EntityManagerFactory build() {
        try {
            emf = Persistence.createEntityManagerFactory("hackathonPU");
            return emf;
        } catch (Throwable ex) {
            System.err.println("Errore nella creazione dell'EntityManagerFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = build();
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
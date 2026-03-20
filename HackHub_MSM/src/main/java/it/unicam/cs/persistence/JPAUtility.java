package it.unicam.cs.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Classe utility per la gestione del ciclo di vita della persistenza JPA.
 * <p>
 * Fornisce un punto di accesso centralizzato per ottenere istanze di
 * {@link EntityManagerFactory} e {@link EntityManager}, implementando un
 * pattern Singleton lazy-initialized per la factory. Questa classe incapsula
 * la logica di inizializzazione della {@link Persistence} utilizzando
 * l'unità di persistenza definita nel file {@code persistence.xml}.
 * </p>
 * <p><strong>Gestione delle risorse:</strong> È responsabilità del chiamante
 * chiudere gli {@link EntityManager} ottenuti tramite {@link #getEntityManager()}.
 * La factory invece deve essere chiusa solo al termine dell'esecuzione dell'applicazione
 * tramite il metodo {@link #close()}.</p>
 * <p><strong>Nota sulla thread-safety:</strong> {@link EntityManagerFactory} è
 * thread-safe e può essere condivisa, mentre {@link EntityManager} NON è thread-safe
 * e deve essere creato per ogni operazione o thread di esecuzione.</p>
 */
public class JPAUtility {

    /**
     * Istanza singleton della {@link EntityManagerFactory}.
     * Viene inizializzata al primo accesso (lazy initialization).
     */
    private static EntityManagerFactory emf;

    /**
     * Costruttore privato per impedire l'istanziazione esterna.
     * Garantisce che questa classe sia utilizzata solo tramite metodi statici.
     */
    private JPAUtility() {}

    /**
     * Inizializza la {@link EntityManagerFactory} caricando l'unità di persistenza
     * configurata nel file {@code persistence.xml} (nome: "hackathonPU").
     * <p>
     * Questo metodo gestisce eventuali errori durante la creazione della factory
     * lanciando un {@link ExceptionInInitializerError} per bloccare l'avvio
     * dell'applicazione in caso di configurazione errata.
     * </p>
     *
     * @return l'istanza appena creata di {@link EntityManagerFactory}
     * @throws ExceptionInInitializerError se la creazione della factory fallisce
     */
    private static EntityManagerFactory build() {
        try {
            emf = Persistence.createEntityManagerFactory("hackathonPU");
            return emf;
        } catch (Throwable ex) {
            System.err.println("Errore nella creazione dell'EntityManagerFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Restituisce l'istanza singleton della {@link EntityManagerFactory}.
     * <p>
     * Se la factory non è stata ancora inizializzata o è stata chiusa,
     * viene invocata automaticamente la procedura di {@link #build()}.
     * </p>
     *
     * @return l'istanza di {@link EntityManagerFactory} attiva
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = build();
        }
        return emf;
    }

    /**
     * Crea e restituisce un nuovo {@link EntityManager} dalla factory.
     * <p>
     * Ogni chiamata a questo metodo restituisce una nuova istanza di
     * {@link EntityManager}, che rappresenta il contesto di persistenza
     * per le operazioni CRUD. Il chiamante è responsabile di chiudere
     * l'entityManager dopo l'uso (tipicamente tramite {@code finally} o
     * try-with-resources).
     * </p>
     *
     * @return una nuova istanza di {@link EntityManager}
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Chiude la {@link EntityManagerFactory} e rilascia tutte le risorse
     * associate (connessioni al database, cache, ecc.).
     * <p>
     * Questo metodo dovrebbe essere invocato esclusivamente durante la
     * fase di shutdown dell'applicazione per garantire una chiusura corretta
     * del contesto di persistenza. Dopo la chiusura, la factory non potrà
     * più essere utilizzata a meno di non essere reinizializzata.
     * </p>
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
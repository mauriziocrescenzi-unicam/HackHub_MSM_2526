package it.unicam.cs.service.facade;

/**
 * Interfaccia che definisce il contratto per la validazione di link a repository di codice sorgente.
 * Ogni implementazione gestisce un provider specifico (es. GitHub, GitLab)
 * e si occupa di verificare sia il supporto che la correttezza sintattica del link.
 */
public interface RepositoryProviderService {
    /**
     * Verifica se questo provider supporta il link fornito.
     * La verifica si basa tipicamente sul dominio del link (es. {@code github.com}, {@code gitlab.com}).
     *
     * @param link il link del repository da verificare
     * @return {@code true} se questo provider è competente per il link, {@code false} altrimenti
     */
    boolean isSupported(String link);

    /**
     * Verifica che il link fornito sia sintatticamente valido secondo le regole del provider.
     *
     * @param link il link del repository da validare
     * @return {@code true} se il link rispetta il formato atteso dal provider, {@code false} altrimenti
     */
    boolean isValid(String link);
}
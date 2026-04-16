package it.unicam.cs.service.facade;


public interface RepositoryProviderService {
    /**
     * verifica se il link è supportato
     * @param link link del repository
     * @return
     */
    boolean isSupported(String link);

    /**
     * verifica che il link sia valido
     * @param link link del repository
     * @return
     */
    boolean isValid(String link);
}
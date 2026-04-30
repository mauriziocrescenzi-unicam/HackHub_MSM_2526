package it.unicam.cs.service.facade;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade per la validazione dei link ai repository di codice sorgente.
 * Delega la verifica al primo {@link RepositoryProviderService} che dichiara di supportare il link fornito.
 * Supporta più provider (es. GitHub, GitLab) tramite iniezione della lista di implementazioni.
 */
@Service
public class RepositoryFacade {


    private final List<RepositoryProviderService> providers;
    /**
     * Costruisce un'istanza di {@code RepositoryFacade} con la lista dei provider disponibili.
     *
     * @param providers lista delle implementazioni di {@link RepositoryProviderService} disponibili
     */
    public RepositoryFacade(List<RepositoryProviderService> providers) {
        this.providers = providers;
    }

    /**
     * Valida che il link fornito appartenga a un provider supportato e sia sintatticamente corretto.
     *
     * @param link il link del repository da validare
     * @return {@code true} se il link è supportato da almeno un provider ed è valido;
     *         {@code false} se il link è {@code null}, blank, non supportato da nessun provider,
     *         o non supera la validazione del provider competente
     */
    public boolean validaLink(String link) {
        if (link == null || link.isBlank())
            return false;

        for (RepositoryProviderService provider : providers) {
            if (provider.isSupported(link)) {
                return provider.isValid(link);
            }
        }

        return false;
    }
}
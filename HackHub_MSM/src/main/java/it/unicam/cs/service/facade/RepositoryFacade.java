package it.unicam.cs.service.facade;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RepositoryFacade {


    private final List<RepositoryProviderService> providers;

    public RepositoryFacade(List<RepositoryProviderService> providers) {
        this.providers = providers;
    }

    /**
     * Valida che il link appartenga a un sistema supportato e sia corretto.
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
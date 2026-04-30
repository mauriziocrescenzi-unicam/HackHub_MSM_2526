package it.unicam.cs.service.facade;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementazione di {@link RepositoryProviderService} per la validazione di link GitLab.
 * Verifica che il link appartenga al dominio {@code gitlab.com} e rispetti il formato atteso.
 */
@Service
public class GitLabService implements RepositoryProviderService {
    private static final Pattern GITLAB_URL = Pattern.compile(
            "^https?://(www\\.)?gitlab\\.com/([\\w.-]+/)+[\\w.-]+/?$"
    );
    /**
     * Verifica se il link appartiene al dominio GitLab.
     *
     * @param link il link da verificare
     * @return {@code true} se il link contiene {@code gitlab.com/}, {@code false} altrimenti
     *         o se {@code link} è {@code null}
     */
    @Override
    public boolean isSupported(String link) {
        return link != null && link.toLowerCase().contains("gitlab.com/");
    }
    /**
     * Verifica che il link GitLab sia sintatticamente valido secondo il pattern atteso.
     * Il link deve essere supportato (contenere {@code gitlab.com/}) e rispettare il formato:
     * {@code https://gitlab.com/<gruppo>/<sottogruppo>/<repository>}.
     *
     * @param link il link da validare
     * @return {@code true} se il link è supportato e corrisponde al pattern GitLab, {@code false} altrimenti
     */
    @Override
    public boolean isValid(String link) {
        if (!isSupported(link)) {
            return false;
        }

        return GITLAB_URL.matcher(link).matches();
    }
}
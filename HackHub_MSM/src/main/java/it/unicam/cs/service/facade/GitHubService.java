package it.unicam.cs.service.facade;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementazione di {@link RepositoryProviderService} per la validazione di link GitHub.
 * Verifica che il link appartenga al dominio {@code github.com} e rispetti il formato atteso.
 */
@Service
public class GitHubService implements RepositoryProviderService {
    private static final Pattern GITHUB_URL = Pattern.compile(
            "^https?://(www\\.)?github\\.com/[a-zA-Z0-9](?:-?[a-zA-Z0-9])*/[\\w.-]+/?$"
    );
    /**
     * Verifica se il link appartiene al dominio GitHub.
     *
     * @param link il link da verificare
     * @return {@code true} se il link contiene {@code github.com/}, {@code false} altrimenti
     *         o se {@code link} è {@code null}
     */
    @Override
    public boolean isSupported(String link) {
        return link != null && link.toLowerCase().contains("github.com/");
    }
    /**
     * Verifica che il link GitHub sia sintatticamente valido secondo il pattern atteso.
     * Il link deve essere supportato (contenere {@code github.com/}) e rispettare il formato:
     * {@code https://github.com/<utente>/<repository>}.
     *
     * @param link il link da validare
     * @return {@code true} se il link è supportato e corrisponde al pattern GitHub, {@code false} altrimenti
     */
    @Override
    public boolean isValid(String link) {
        if (!isSupported(link)) {
            return false;
        }
        return GITHUB_URL.matcher(link).matches();
    }
}
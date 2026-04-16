package it.unicam.cs.service.facade;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementazione per la validazione di link GitHub.
 */
@Service
public class GitHubService implements RepositoryProviderService {
    private static final Pattern GITHUB_URL = Pattern.compile(
            "^https?://(www\\.)?github\\.com/[a-zA-Z0-9](?:-?[a-zA-Z0-9])*/[\\w.-]+/?$"
    );
    @Override
    public boolean isSupported(String link) {
        return link != null && link.toLowerCase().contains("github.com/");
    }

    @Override
    public boolean isValid(String link) {
        if (!isSupported(link)) {
            return false;
        }
        return GITHUB_URL.matcher(link).matches();
    }
}
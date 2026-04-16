package it.unicam.cs.service.facade;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementazione per la validazione di link GitLab.
 */
@Service
public class GitLabService implements RepositoryProviderService {
    private static final Pattern GITLAB_URL = Pattern.compile(
            "^https?://(www\\.)?gitlab\\.com/([\\w.-]+/)+[\\w.-]+/?$"
    );

    @Override
    public boolean isSupported(String link) {
        return link != null && link.toLowerCase().contains("gitlab.com/");
    }

    @Override
    public boolean isValid(String link) {
        if (!isSupported(link)) {
            return false;
        }

        return GITLAB_URL.matcher(link).matches();
    }
}
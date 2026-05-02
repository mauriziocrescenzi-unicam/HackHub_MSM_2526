package it.unicam.cs.dto;
/**
 * DTO per la richiesta di registrazione di un nuovo account.
 *
 * @param email    l'email univoca del nuovo account
 * @param password la password in chiaro (verrà cifrata prima del salvataggio)
 * @param nome     il nome dell'utente
 * @param cognome  il cognome dell'utente
 * @param ruolo    il ruolo da assegnare all'account (es. {@code "UTENTE"} o {@code "STAFF"})
 */
public record RegisterRequest(
        String email,
        String password,
        String nome,
        String cognome,
        String ruolo
) {}

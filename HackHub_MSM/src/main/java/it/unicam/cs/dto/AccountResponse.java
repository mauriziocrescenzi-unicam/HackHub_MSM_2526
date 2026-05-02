package it.unicam.cs.dto;
/**
 * DTO di risposta contenente le informazioni principali di un account utente.
 *
 * @param id      l'ID univoco dell'account
 * @param email   l'email dell'account
 * @param nome    il nome dell'utente
 * @param cognome il cognome dell'utente
 * @param ruolo   il ruolo dell'account (es. {@code "UTENTE"} o {@code "STAFF"})
 */
public record AccountResponse(Long id, String email, String nome, String cognome, String ruolo) {
}

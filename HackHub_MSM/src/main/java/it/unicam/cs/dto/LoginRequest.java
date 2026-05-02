package it.unicam.cs.dto;
/**
 * DTO per la richiesta di login.
 *
 * @param email    l'email dell'account con cui autenticarsi
 * @param password la password in chiaro dell'account
 */
public record LoginRequest(String email, String password) {
}

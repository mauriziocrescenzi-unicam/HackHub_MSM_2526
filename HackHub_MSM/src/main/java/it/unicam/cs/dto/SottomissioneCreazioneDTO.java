package it.unicam.cs.dto;

/**
 * DTO per la richiesta di invio o aggiornamento di una sottomissione.
 *
 * @param nome        il nome della sottomissione
 * @param link        il link al progetto (GitHub, GitLab, ecc.)
 * @param idHackathon l'ID dell'hackathon a cui è destinata la sottomissione
 */
public record SottomissioneCreazioneDTO(
        String nome,
        String link,
        long idHackathon
) {
}

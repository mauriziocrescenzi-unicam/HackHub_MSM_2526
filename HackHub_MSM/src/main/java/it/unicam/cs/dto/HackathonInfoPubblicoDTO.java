package it.unicam.cs.dto;

import it.unicam.cs.model.Hackathon;
/**
 * DTO di risposta contenente le sole informazioni pubbliche di un hackathon.
 * Restituito agli utenti non autenticati o anonimi al posto del DTO completo.
 *
 * @param nome        il nome dell'hackathon
 * @param regolamento il testo del regolamento
 */
public record HackathonInfoPubblicoDTO(String nome, String regolamento) {
    /**
     * Crea un DTO dalle informazioni pubbliche di un'entità {@link Hackathon}.
     *
     * @param h l'entità hackathon da cui estrarre i dati
     * @return un nuovo {@link HackathonInfoPubblicoDTO} con nome e regolamento
     */
    public static HackathonInfoPubblicoDTO fromHackathon(Hackathon h) {
        return new HackathonInfoPubblicoDTO(h.getNome(), h.getRegolamento());
    }
}
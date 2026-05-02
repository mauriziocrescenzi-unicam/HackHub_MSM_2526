package it.unicam.cs.dto;

import it.unicam.cs.model.Invito;
import it.unicam.cs.model.StatoInvito;
import java.time.LocalDateTime;
/**
 * DTO di risposta contenente le informazioni di un invito a unirsi a un team.
 *
 * @param id                   l'ID univoco dell'invito
 * @param idUtenteMittente     l'ID dell'utente che ha inviato l'invito
 * @param idUtenteDestinatario l'ID dell'utente che ha ricevuto l'invito
 * @param dataInvio            la data e ora in cui l'invito è stato inviato
 * @param stato                lo stato corrente dell'invito
 */
public record InvitoRispostaDTO(
        long id,
        int idUtenteMittente,
        int idUtenteDestinatario,
        LocalDateTime dataInvio,
        StatoInvito stato
) {
    /**
     * Crea un DTO a partire da un'entità {@link Invito}.
     *
     * @param i l'entità invito da cui estrarre i dati
     * @return un nuovo {@link InvitoRispostaDTO} con tutti i campi popolati
     */
    public static InvitoRispostaDTO fromInvito(Invito i) {
        return new InvitoRispostaDTO(
                i.getId(),
                i.getIdUtenteMittente(),
                i.getIdUtenteDestinatario(),
                i.getDataInvio(),
                i.getStato()
        );
    }
}
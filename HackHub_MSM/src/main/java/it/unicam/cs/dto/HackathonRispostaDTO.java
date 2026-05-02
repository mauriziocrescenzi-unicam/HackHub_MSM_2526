package it.unicam.cs.dto;

import it.unicam.cs.model.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
/**
 * DTO di risposta contenente le informazioni complete di un hackathon.
 * Restituito agli utenti autenticati; espone email di organizzatore, giudice e mentori
 * senza esporre direttamente le entità JPA.
 *
 * @param nome                  il nome dell'hackathon
 * @param regolamento           il testo del regolamento
 * @param scadenzaIscrizione    la data e ora di scadenza delle iscrizioni
 * @param dataInizio            la data e ora di inizio
 * @param dataFine              la data e ora di fine
 * @param luogo                 il luogo dell'evento
 * @param premioInDenaro        il premio in denaro per i vincitori
 * @param dimensioneMassimoTeam il numero massimo di membri per team
 * @param stato                 lo stato corrente dell'hackathon
 * @param organizzatore         l'email dell'organizzatore
 * @param giudice               l'email del giudice
 * @param mentori               la lista delle email dei mentori assegnati
 */
public record HackathonRispostaDTO(
        @NotNull String nome,
        @NotNull String regolamento,
        @NotNull LocalDateTime scadenzaIscrizione,
        @NotNull LocalDateTime dataInizio,
        @NotNull LocalDateTime dataFine,
        @NotNull String luogo,
        @NotNull Double premioInDenaro,
        @NotNull Integer dimensioneMassimoTeam,
        @NotNull StatoHackathon stato,
        @NotNull String organizzatore,
        @NotNull String giudice,
        @NotNull List<String> mentori
) {
    /**
     * Crea un DTO completo a partire da un'entità {@link Hackathon}.
     *
     * @param hackathon l'entità hackathon da cui estrarre i dati
     * @return un nuovo {@link HackathonRispostaDTO} con tutti i campi popolati
     */
    public static HackathonRispostaDTO fromHackathon(Hackathon hackathon) {
        return new HackathonRispostaDTO(hackathon.getNome(),hackathon.getRegolamento(),hackathon.getScadenzaIscrizione(),
                hackathon.getDataInizio(),hackathon.getDataFine(),hackathon.getLuogo(),hackathon.getPremioInDenaro(),
                hackathon.getDimensioneMassimoTeam(),hackathon.getStato(),hackathon.getOrganizzatore().getEmail(),hackathon.getGiudice().getEmail(),
                hackathon.getMentori().stream().map(Account::getEmail).toList());
    }
}

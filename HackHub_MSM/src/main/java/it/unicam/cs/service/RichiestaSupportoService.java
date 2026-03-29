package it.unicam.cs.service;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.RichiestaSupporto;
import it.unicam.cs.model.Team;
import it.unicam.cs.persistence.StandardPersistence;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsabile della gestione delle richieste di supporto
 * tra team e mentori nel sistema HackHub.
 * <p>
 * Implementa il pattern Singleton per garantire un'unica istanza del service.
 * Gestisce tutte le operazioni relative alle richieste di supporto.
 * </p>
 */
public class RichiestaSupportoService {

    private static RichiestaSupportoService service;
    private final StandardPersistence<RichiestaSupporto> richiestaSupportoPersistence;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza il layer di persistenza per l'entità RichiestaSupporto.
     */
    private RichiestaSupportoService() {
        this.richiestaSupportoPersistence = new StandardPersistence<>(RichiestaSupporto.class);
    }

    /**
     * Restituisce l'istanza Singleton del RichiestaSupportoService.
     * Crea una nuova istanza se non esiste ancora.
     *
     * @return Istanza Singleton di RichiestaSupportoService
     */
    public static RichiestaSupportoService getInstance() {
        if (service == null) {
            service = new RichiestaSupportoService();
        }
        return service;
    }

    /**
     * Invia una nuova richiesta di supporto da parte di un team.
     *
     * @param teamRichiedente Team che richiede supporto
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     * @param dataInvio Data e ora di invio della richiesta
     * @return La richiesta di supporto creata, o null se la creazione fallisce
     */
    public RichiestaSupporto inviaRichiestaSupporto(Team teamRichiedente, String descrizioneRichiesta, LocalDateTime dataInvio) {
        if (!verificaRichiestaSupporto(teamRichiedente, descrizioneRichiesta, dataInvio)) {
            return null;
        }

        RichiestaSupporto richiesta = new RichiestaSupporto(descrizioneRichiesta);
        richiesta.setDataInvio(dataInvio);

        // Nota: nel modello attuale non ci sono riferimenti a Team/Mentore/Hackathon
        // Se necessario, questi andrebbero aggiunti all'entità RichiestaSupporto

        richiestaSupportoPersistence.create(richiesta);
        return richiesta;
    }

    /**
     * Verifica la validità di una richiesta di supporto prima dell'invio.
     *
     * @param teamRichiedente Team che richiede supporto
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     * @param dataInvio Data e ora di invio della richiesta
     * @return true se la richiesta è valida, false altrimenti
     */
    public boolean verificaRichiestaSupporto(Team teamRichiedente, String descrizioneRichiesta, LocalDateTime dataInvio) {
        if (teamRichiedente == null) {
            return false;
        }
        if (descrizioneRichiesta == null || descrizioneRichiesta.trim().isEmpty()) {
            return false;
        }
        if (dataInvio == null) {
            return false;
        }
        if (dataInvio.isAfter(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    /**
     * Restituisce tutte le richieste di supporto per un determinato hackathon.
     *
     * @param hackathonSelezionato Hackathon di cui recuperare le richieste
     * @return Lista di richieste di supporto, o lista vuota se nessuna presente
     */
    public List<RichiestaSupporto> getRichiesteSupporto(Hackathon hackathonSelezionato) {
        if (hackathonSelezionato == null) {
            return new ArrayList<>();
        }

        return richiestaSupportoPersistence.getAll();
    }

    /**
     * Restituisce una specifica richiesta di supporto.
     *
     * @param richiestaSupportoSelezionata La richiesta da recuperare
     * @return La richiesta trovata, o null se non esiste
     */
    public RichiestaSupporto getRichiestaSupporto(Long richiestaSupportoSelezionata) {
        if (richiestaSupportoSelezionata == null) {
            return null;
        }

        // Cerca la richiesta nel database
        List<RichiestaSupporto> tutteRichieste = richiestaSupportoPersistence.getAll();
        for (RichiestaSupporto r : tutteRichieste) {
            if (r.equals(richiestaSupportoSelezionata)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Verifica se una richiesta di supporto è stata risolta.
     * Una richiesta è considerata risolta se ha una descrizione di risposta non vuota.
     *
     * @param richiestaSupporto La richiesta da verificare
     * @return true se la richiesta è stata risolta, false altrimenti
     */
    public boolean isRichiestaSupportoRisolta(RichiestaSupporto richiestaSupporto) {
        if (richiestaSupporto == null) {
            return false;
        }

        String descrizioneRisposta = richiestaSupporto.getDescrizioneRisposta();
        return descrizioneRisposta != null && !descrizioneRisposta.trim().isEmpty();
    }

    /**
     * Risponde a una richiesta di supporto con una descrizione.
     *
     * @param richiestaSupporto La richiesta a cui rispondere
     * @param descrizioneRisposta Descrizione della risposta
     * @return true se la risposta è stata salvata con successo, false altrimenti
     */
    public boolean rispostaRichiestaSupporto(RichiestaSupporto richiestaSupporto, String descrizioneRisposta) {
        if (richiestaSupporto == null) {
            return false;
        }

        if (!checkRisposta(descrizioneRisposta)) {
            return false;
        }

        richiestaSupporto.setDescrizioneRisposta(descrizioneRisposta);
        richiestaSupportoPersistence.update(richiestaSupporto);
        return true;
    }

    /**
     * Verifica la validità di una descrizione di risposta.
     *
     * @param descrizioneRisposta Descrizione della risposta da verificare
     * @return true se la descrizione è valida, false altrimenti
     */
    public boolean checkRisposta(String descrizioneRisposta) {
        // Verifica che la descrizione non sia nulla o vuota
        if (descrizioneRisposta == null || descrizioneRisposta.trim().isEmpty()) {
            return false;
        }

        return true;
    }
}
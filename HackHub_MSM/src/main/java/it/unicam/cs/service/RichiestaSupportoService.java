package it.unicam.cs.service;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.RichiestaSupporto;
import it.unicam.cs.model.Team;
import it.unicam.cs.repository.RichiestaSupportoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Service
@Transactional
public class RichiestaSupportoService {

    private final RichiestaSupportoRepository richiestaSupportoRepository;

    public RichiestaSupportoService(RichiestaSupportoRepository richiestaSupportoRepository) {
        this.richiestaSupportoRepository = richiestaSupportoRepository;
    }

    // USE CASE: Invio Richiesta di Supporto (MembroTeam)
    /**
     * Invia una nuova richiesta di supporto da parte di un team.
     * Corrisponde alla chiamata inviaRichiestaSupporto() nel sequence diagram.
     *
     * @param idTeamRichiedente ID del team che richiede supporto
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     * @param dataInvio Data e ora di invio della richiesta
     * @param hackathon Hackathon nel cui contesto viene inviata la richiesta
     * @return La richiesta di supporto creata, o null se la validazione fallisce
     */
    public RichiestaSupporto inviaRichiestaSupporto(Long idTeamRichiedente,
                                                    String descrizioneRichiesta,
                                                    LocalDateTime dataInvio,
                                                    Hackathon hackathon) {
        if (!verificaRichiestaSupporto(idTeamRichiedente, descrizioneRichiesta, dataInvio, hackathon)) {
            return null;
        }

        RichiestaSupporto richiesta = new RichiestaSupporto(
                descrizioneRichiesta,
                idTeamRichiedente,
                dataInvio,
                hackathon
        );

        return richiestaSupportoRepository.save(richiesta);
    }

    /**
     * Verifica la validità di una richiesta di supporto prima dell'invio.
     * Corrisponde a verificaRichiestaSupporto() nel sequence diagram.
     *
     * @param idTeamRichiedente ID del team richiedente
     * @param descrizioneRichiesta Descrizione del supporto richiesto
     * @param dataInvio Data e ora di invio
     * @param hackathon Hackathon di riferimento
     * @return true se la richiesta è valida, false altrimenti
     */
    public boolean verificaRichiestaSupporto(Long idTeamRichiedente,
                                             String descrizioneRichiesta,
                                             LocalDateTime dataInvio,
                                             Hackathon hackathon) {
        if (idTeamRichiedente == null || idTeamRichiedente <= 0) {
            return false;
        }
        if (descrizioneRichiesta == null || descrizioneRichiesta.trim().isEmpty()) {
            return false;
        }
        if (dataInvio == null || dataInvio.isAfter(LocalDateTime.now())) {
            return false;
        }
        if (hackathon == null) {
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

        return richiestaSupportoRepository.findByHackathon(hackathonSelezionato);
    }

    /**
     * Restituisce una specifica richiesta di supporto tramite il suo ID.
     *
     * @param idRichiesta ID della richiesta da recuperare
     * @return La richiesta trovata, o null se non esiste
     */
    public RichiestaSupporto getRichiestaSupporto(Long idRichiesta) {
        if (idRichiesta == null) {
            return null;
        }
        return richiestaSupportoRepository.findById(idRichiesta).orElse(null);
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
     * Risponde a una richiesta di supporto.
     * Corrisponde a rispostaRichiestaSupporto(risposta) nel sequence diagram.
     * Internamente esegue checkRisposta() e setRisposta() sull'oggetto.
     *
     * @param richiestaSupporto La richiesta a cui rispondere
     * @param descrizioneRisposta Testo della risposta
     * @return true se la risposta è stata salvata con successo, false altrimenti
     */
    public boolean rispostaRichiestaSupporto(RichiestaSupporto richiestaSupporto,
                                             String descrizioneRisposta) {
        if (richiestaSupporto == null) {
            return false;
        }
        if (!checkRisposta(descrizioneRisposta)) {
            return false;
        }
        richiestaSupporto.setDescrizioneRisposta(descrizioneRisposta);
        richiestaSupportoRepository.save(richiestaSupporto);
        return true;
    }

    /**
     * Verifica la validità di una descrizione di risposta.
     *
     * @param descrizioneRisposta Descrizione della risposta da verificare
     * @return true se la descrizione è valida, false altrimenti
     */
    public boolean checkRisposta(String descrizioneRisposta) {
        if (descrizioneRisposta == null || descrizioneRisposta.trim().isEmpty()) {
            return false;
        }

        return true;
    }
}
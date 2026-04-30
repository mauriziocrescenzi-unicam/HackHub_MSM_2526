package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.AccountRepository;
import it.unicam.cs.repository.HackathonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
/**
 * Service per la gestione dei mentori nel sistema HackHub.
 * Fornisce operazioni per assegnare mentori agli hackathon, gestire le richieste
 * di supporto e recuperare le informazioni sui mentori.
 */
@Service
@Transactional
public class MentoreService {
    private final AccountRepository repository;
    private final HackathonRepository hackathonRepository;
    private final RichiestaSupportoService richiestaSupportoService;
    /**
     * Costruisce un'istanza di {@code MentoreService} con le dipendenze necessarie.
     *
     * @param repository               repository per l'accesso agli account
     * @param hackathonRepository      repository per l'accesso agli hackathon
     * @param richiestaSupportoService service per la gestione delle richieste di supporto
     */
    public MentoreService(AccountRepository repository,
                          HackathonRepository hackathonRepository,
                          RichiestaSupportoService richiestaSupportoService) {
        this.repository = repository;
        this.hackathonRepository = hackathonRepository;
        this.richiestaSupportoService = richiestaSupportoService;
    }

    /**
     * Verifica la validità di una lista di mentori.
     * Controlla che la lista non sia nulla o vuota, che non contenga elementi {@code null}
     * e che non siano presenti duplicati.
     *
     * @param mentori lista di account da verificare
     * @return {@code true} se la lista è valida
     * @throws IllegalArgumentException se la lista è nulla/vuota, contiene elementi {@code null} o duplicati
     */
    public static boolean verificaMentore(List<Account> mentori){
        if (mentori == null || mentori.isEmpty())
            throw new IllegalArgumentException("La lista dei mentori non può essere nulla o vuota");

        if (mentori.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("La lista non può contenere mentori nulli");

        if (mentori.stream().map(Account::getId).distinct().count() != mentori.size())
            throw new IllegalArgumentException("La lista contiene mentori duplicati");

        return true;
    }


    /**
     * Aggiunge più mentori a un hackathon, mantenendo quelli già assegnati.
     * I mentori duplicati vengono ignorati
     *
     * @param mentori   lista degli ID dei mentori da aggiungere; non può essere {@code null}
     * @param hackathon l'hackathon a cui aggiungere i mentori; non può essere {@code null}
     * @return {@code true} se l'aggiunta è avvenuta con successo, {@code false} se
     *         almeno uno degli ID non corrisponde a nessun account
     * @throws NullPointerException se {@code hackathon} o {@code mentori} sono {@code null}
     */
    public boolean aggiungiMentori(List<Long> mentori,Hackathon hackathon){
        if(hackathon == null) throw new NullPointerException("Hackathon non valido");
        if (mentori == null) throw new NullPointerException("La lista dei mentori non può essere nulla");
        //prendo i mentori dalla lista
        List<Account> mentoriDaAggiungere = mentori.stream().map(this::getMentoreById).toList();
        //controllo che non ci siano mentori nulli
        if (mentoriDaAggiungere.stream().anyMatch(Objects::isNull)) return false;
        verificaMentore(mentoriDaAggiungere);
        //aggiungo i mentori al hackathon
        Set<Account> mentoriEsistenti = new HashSet<>(hackathon.getMentori());
        mentoriEsistenti.addAll(mentoriDaAggiungere);
        hackathon.setMentori(new ArrayList<>(mentoriEsistenti));
        hackathonRepository.save(hackathon);
        return true;
    }


    /**
     * Restituisce l'account del mentore con l'ID specificato.
     *
     * @param idMentore l'ID del mentore da cercare
     * @return l'account trovato, oppure {@code null} se non esiste
     */
    public Account getMentoreById(long idMentore) {
        return repository.findById(idMentore).orElse(null);
    }
    /**
     * Restituisce la lista delle richieste di supporto relative a un hackathon.
     *
     * @param hackathon l'hackathon di cui recuperare le richieste
     * @return lista delle {@link RichiestaSupporto} associate all'hackathon
     */
    public List<RichiestaSupporto> getRichiesteSupporto(Hackathon hackathon) {
        return richiestaSupportoService.getRichiesteSupporto(hackathon);
    }
    /**
     * Restituisce la richiesta di supporto con l'ID specificato.
     *
     * @param idRichiesta l'ID della richiesta da recuperare
     * @return la {@link RichiestaSupporto} trovata, oppure {@code null} se non esiste
     */
    public RichiestaSupporto getRichiestaSupporto(Long idRichiesta) {
        return richiestaSupportoService.getRichiestaSupporto(idRichiesta);
    }

    /**
     * Verifica se una richiesta è già stata risolta.
     * Corrisponde a isRichiestaSupportoRisolta() nel sequence diagram.
     *
     * @param richiestaSupporto La richiesta da verificare
     * @return true se già risolta, false altrimenti
     */
    public boolean isRichiestaSupportoRisolta(RichiestaSupporto richiestaSupporto) {
        return richiestaSupportoService.isRichiestaSupportoRisolta(richiestaSupporto);
    }
    /**
     * Risponde a una richiesta di supporto.
     * Corrisponde a rispostaRichiestaSupporto(risposta) nel sequence diagram.
     * Delega la logica (checkRisposta + setRisposta) al RichiestaSupportoService.
     *
     * @param richiestaSupporto   La richiesta a cui rispondere
     * @param descrizioneRisposta Testo della risposta
     * @return true se la risposta è valida e salvata, false altrimenti
     */
    public boolean rispostaRichiestaSupporto(RichiestaSupporto richiestaSupporto,
                                             String descrizioneRisposta) {
        return richiestaSupportoService.rispostaRichiestaSupporto(richiestaSupporto, descrizioneRisposta);
    }
}

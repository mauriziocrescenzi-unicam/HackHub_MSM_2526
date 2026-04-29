package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
/**
 * Service per la gestione degli hackathon.
 * Fornisce operazioni di creazione, modifica, recupero e verifica degli hackathon nel sistema.
 */
@Service
@Transactional
public class HackathonService {
    private final HackathonRepository repository;
    private final AccountRepository accountRepository;


    /**
     * Costruisce un'istanza di {@code HackathonService} con le dipendenze necessarie.
     *
     * @param repository        repository per l'accesso agli hackathon
     * @param accountRepository repository per l'accesso agli account
     */
    public HackathonService(HackathonRepository repository, AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;

    }
    /**
     * Crea e salva un nuovo hackathon nel sistema.
     * Verifica che le date siano valide e che tutti i partecipanti (organizzatore,
     * giudice e mentori) esistano e abbiano il ruolo {@link Ruolo#STAFF}.
     *
     * @param nome                  il nome dell'hackathon
     * @param regolamento           il testo del regolamento
     * @param scadenzaIscrizione    la data e ora di scadenza delle iscrizioni
     * @param dataInizio            la data e ora di inizio dell'hackathon
     * @param dataFine              la data e ora di fine dell'hackathon
     * @param luogo                 il luogo in cui si svolge l'hackathon
     * @param premioInDenaro        il premio in denaro per i vincitori
     * @param dimensioneMassimoTeam il numero massimo di membri per team
     * @param stato                 lo stato iniziale dell'hackathon
     * @param idorganizzatore       l'ID dell'account organizzatore (deve avere ruolo STAFF)
     * @param idgiudice             l'ID dell'account giudice (deve avere ruolo STAFF)
     * @param idmentori             la lista degli ID degli account mentori (devono avere ruolo STAFF)
     * @return {@code true} se l'hackathon è stato creato con successo, {@code false} altrimenti
     */
    public boolean creaHackathon(String nome, String regolamento, LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio,
                                 LocalDateTime dataFine, String luogo, double premioInDenaro, int dimensioneMassimoTeam,
                                 StatoHackathon stato, Long idorganizzatore, Long idgiudice, List<Long> idmentori){
        if(!verificaRequisiti(scadenzaIscrizione,dataInizio,dataFine)) return false;
        Builder builder = new HackathonBuilder();
        builder.reset();
        Account organizzatore = accountRepository.findById(idorganizzatore)
                .orElse(null);
        if(organizzatore == null) return false;
        if(organizzatore.getRuolo()!= Ruolo.STAFF)return false;
        Account giudice = accountRepository.findById(idgiudice)
                .orElse(null);
        if(giudice == null) return false;
        if(giudice.getRuolo()!= Ruolo.STAFF)return false;
        List<Account> mentori = accountRepository.findAllById(idmentori);
        if(mentori.isEmpty()) return false;
        if(mentori.stream().anyMatch(m -> m.getRuolo()!=Ruolo.STAFF)) return false;
        builder.setInfo(nome,regolamento,scadenzaIscrizione,dataInizio,dataFine,luogo,premioInDenaro,
                dimensioneMassimoTeam,stato,organizzatore);
        builder.setGiudice(giudice);
        builder.setMentori(mentori);
        Hackathon hackathon = builder.getResult();
        repository.save(hackathon);
        return true;
    }

    /**
     * Verifica che le date fornite siano valide per la creazione o modifica di un hackathon.
     * Le date devono essere tutte future rispetto al momento corrente; inoltre la scadenza
     * delle iscrizioni deve precedere l'inizio, e l'inizio non deve essere successivo alla fine.
     *
     * @param scadenzaIscrizione la data di scadenza delle iscrizioni
     * @param dataInizio         la data di inizio dell'hackathon
     * @param dataFine           la data di fine dell'hackathon
     * @return {@code true} se tutte le date sono valide, {@code false} altrimenti
     */
    public boolean verificaRequisiti( LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio, LocalDateTime dataFine){
      if(scadenzaIscrizione.isBefore(LocalDateTime.now()) || dataInizio.isBefore(LocalDateTime.now())
               || dataFine.isBefore(LocalDateTime.now())) return false;
       if (!scadenzaIscrizione.isBefore(dataInizio)) return false;
        return !dataInizio.isAfter(dataFine);
    }

    /**
     * Restituisce l'hackathon con l'ID specificato.
     *
     * @param idHackathon l'ID dell'hackathon da cercare
     * @return l'hackathon trovato, oppure {@code null} se non esiste
     */
    public Hackathon getHackathonByID(long idHackathon) {
        return repository.findById(idHackathon).orElse(null);
    }
    /**
     * Restituisce lo stato corrente dell'hackathon specificato.
     *
     * @param hackathon l'hackathon di cui recuperare lo stato
     * @return lo stato corrente dell'hackathon
     */
    public StatoHackathon getStatoHackathon(Hackathon hackathon) {
        return hackathon.getStato();
    }

    /**
     * Verifica se gli hackathon nella lista si trova in uno degli stati specificati.
     *
     * @param listaHackathon lista di hackathon da verificare; i valori {@code null} vengono ignorati
     * @param stati          array di stati ammessi da verificare
     * @return {@code true} hackathon è in uno degli stati specificati,
     *         {@code false} se la lista o l'array sono null/vuoti, oppure nessun hackathon corrisponde
     */
    public boolean checkStato(List<Hackathon> listaHackathon, StatoHackathon... stati) {
        if (listaHackathon == null || listaHackathon.isEmpty() || stati == null || stati.length == 0) {
            return false;
        }

        Set<StatoHackathon> statiSet = Set.of(stati);
        return listaHackathon.stream()
                .filter(Objects::nonNull)
                .anyMatch(h -> statiSet.contains(h.getStato()));
    }
    /**
     * Modifica i dati principali di un hackathon esistente e li salva nel sistema.
     * Verifica che le nuove date siano valide prima di applicare le modifiche.
     *
     * @param hackathon          l'hackathon da modificare; se {@code null} restituisce {@code false}
     * @param nome               il nuovo nome dell'hackathon
     * @param regolamento        il nuovo regolamento
     * @param scadenzaIscrizione la nuova data di scadenza delle iscrizioni
     * @param dataInizio         la nuova data di inizio
     * @param dataFine           la nuova data di fine
     * @param luogo              il nuovo luogo
     * @param premioInDenaro     il nuovo premio in denaro
     * @return {@code true} se la modifica è avvenuta con successo, {@code false} altrimenti
     */
    public boolean modificaHackathon(Hackathon hackathon,String nome,String regolamento,LocalDateTime scadenzaIscrizione,LocalDateTime dataInizio,LocalDateTime dataFine,String luogo,double premioInDenaro){
        if(hackathon == null) return false;
        if(!verificaRequisiti(scadenzaIscrizione,dataInizio,dataFine)) return false;
        hackathon.setNome(nome);
        hackathon.setRegolamento(regolamento);
        hackathon.setScadenzaIscrizione(scadenzaIscrizione);
        hackathon.setDataInizio(dataInizio);
        hackathon.setDataFine(dataFine);
        hackathon.setLuogo(luogo);
        hackathon.setPremioInDenaro(premioInDenaro);
        repository.save(hackathon);
        return true;

    }

    /**
     * Recupera la lista di tutti gli hackathon presenti nel sistema.
     *
     * @return lista di tutti gli hackathon; vuota se non ne esistono
     */
    public List<Hackathon> getAllListaHackathon() {
        return repository.findAll();
    }



}
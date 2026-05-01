package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service per la gestione dei membri dello staff nel sistema HackHub.
 * Fornisce operazioni per recuperare gli hackathon associati a un membro dello staff,
 * sia in base al ruolo (organizzatore, giudice, mentore) che allo stato dell'hackathon.
 */
@Service
@Transactional
public class MembroDelloStaffService {

    private final HackathonRepository hackathonRepository;
    private final AccountRepository repository;

    /**
     * Costruisce un'istanza di {@code MembroDelloStaffService} con le dipendenze necessarie.
     *
     * @param hackathonRepository  repository per l'accesso agli hackathon
     */
    public MembroDelloStaffService(HackathonRepository hackathonRepository, AccountRepository repository) {
        this.hackathonRepository = hackathonRepository;
        this.repository = repository;
    }

    /**
     * Restituisce la lista degli hackathon di un determinato organizzatore in base allo stato
     * @param stato stato degli hackathon
     * @param id id del membro dello staff
     * @return lista degli hackathon
     */
    public List<Hackathon> getListaHackathons(StatoHackathon stato,long id){
        if(stato == null) throw new NullPointerException("Stato dell'hackathon non valido");
        if(id < 0) throw new IllegalArgumentException("Id dell'organizzatore non valido");
        return hackathonRepository.findAll().stream()
                .filter(h -> h.getOrganizzatore().getId() == id && h.getStato() == stato)
                .toList();
    }
    /**
     * Restituisce la lista degli hackathon a cui è assegnato un membro dello staff.
     *
     * @param idMembro ID del membro dello staff
     * @return Lista di hackathon a cui il membro è assegnato, o lista vuota se non trovato
     */
    public List<Hackathon> getListaHackathon(Long idMembro) {
        if (idMembro == null || idMembro <= 0) {
            throw new IllegalArgumentException("ID del membro dello staff non valido.");
        }
        List<Hackathon> risultato = new ArrayList<>();
        // Cerca hackathon dove è organizzatore
        risultato.addAll(hackathonRepository.findByOrganizzatoreId(idMembro));
        // Cerca hackathon dove è giudice
        risultato.addAll(hackathonRepository.findByGiudiceId(idMembro));
        // Cerca hackathon dove è mentore
        risultato.addAll(hackathonRepository.findByMentoriId(idMembro));
        // Rimuovi duplicati (nel caso raro in cui un membro abbia più ruoli nello stesso hackathon)
        return risultato.stream().distinct().collect(Collectors.toList());
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
        if(mentori.stream()
                .anyMatch(account -> account.getRuolo() == Ruolo.UTENTE))
            throw new IllegalArgumentException("La lista contiene account non mentori");

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
        List<Account> mentoriDaAggiungere = mentori.stream().map(this::getMembroStaffById).toList();
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
    public Account getMembroStaffById(long idMentore) {
        return repository.findById(idMentore).orElse(null);
    }

}
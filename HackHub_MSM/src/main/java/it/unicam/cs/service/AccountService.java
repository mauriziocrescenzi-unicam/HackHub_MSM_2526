package it.unicam.cs.service;

import it.unicam.cs.model.Account;
import it.unicam.cs.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service per la gestione degli account utente.
 * Fornisce operazioni di ricerca e verifica sugli account registrati nel sistema.
 */
@Service
public class AccountService {
    private final AccountRepository repository;
    private final MembroTeamService membroTeamService;

    /**
     * Costruisce un'istanza di {@code AccountService} con le dipendenze necessarie.
     *
     * @param repository       repository per l'accesso agli account
     * @param membroTeamService service per la gestione dei membri del team
     */
    public AccountService(AccountRepository repository, MembroTeamService membroTeamService) {
        this.repository = repository;
        this.membroTeamService = membroTeamService;
    }
    /**
     * Verifica se esiste un account con l'email specificata.
     *
     * @param email l'email da cercare
     * @return {@code true} se l'account esiste, {@code false} altrimenti
     */
    public boolean isPresent(String email) {
        return repository.findByEmail(email).isPresent();
    }
    /**
     * Restituisce l'account associato all'email specificata.
     *
     * @param email l'email dell'account da cercare; non può essere {@code null}
     * @return l'account trovato, oppure {@code null} se non esiste
     * @throws IllegalArgumentException se {@code email} è {@code null}
     */
    public Account find(String email){
        if(email == null) throw new IllegalArgumentException();
        return repository.findByEmail(email).orElse(null);
    }
    /**
     * Restituisce l'ID dell'account associato all'email specificata.
     *
     * @param email l'email dell'account
     * @return l'ID dell'account, oppure {@code null} se non trovato
     */
    public Long findId(String email){
        return repository.findByEmail(email).map(Account::getId).orElse(null);
    }
    /**
     * Restituisce l'account con l'ID specificato.
     *
     * @param id l'ID dell'account da cercare
     * @return l'account trovato, oppure {@code null} se non esiste
     */
    public Account findById(Long id){
        return repository.findById(id).orElse(null);
    }
    /**
     * Verifica se l'account specificato è membro di un team.
     *
     * @param account l'account da verificare
     * @return {@code true} se l'account è membro di un team, {@code false} altrimenti
     */
    public boolean isMembroTeam(Account account) {
        return membroTeamService.isMembroTeam(account.getId());
    }


}

package it.unicam.cs.service;

import it.unicam.cs.model.Account;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Ruolo;
import it.unicam.cs.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final MembroTeamService membroTeamService;


    public AccountService(AccountRepository repository, MembroTeamService membroTeamService) {
        this.repository = repository;
        this.membroTeamService = membroTeamService;
    }
    public boolean isPresent(String email) {
        return repository.findByEmail(email).isPresent();
    }
    public Account find(String email){
        if(email == null) throw new IllegalArgumentException();
        return repository.findByEmail(email).orElse(null);
    }
    public Long findId(String email){
        return repository.findByEmail(email).map(Account::getId).orElse(null);
    }
    public Account findById(Long id){
        return repository.findById(id).orElse(null);
    }

    public boolean isMembroTeam(Account account) {
        return membroTeamService.isMembroTeam(account.getId());
    }

    /**
     * Restituisce la lista di tutti gli account con ruolo STAFF.
     * Nel dominio HackHub, "Giudice" non è un'entità separata ma un Account con ruolo STAFF.
     */
    public List<Account> getListaGiudici() {
        return repository.findByRuolo(Ruolo.STAFF);
    }

    /**
     * Verifica se l'account esiste ed ha il ruolo STAFF.
     * Un giudice deve necessariamente essere un membro dello STAFF.
     */
    public boolean isValidGiudice(Long id) {
        if (id == null || id <= 0) return false;
        Account account = repository.findById(id).orElse(null);
        // Deve esistere E avere obbligatoriamente il ruolo STAFF
        return account != null && account.getRuolo() == Ruolo.STAFF;
    }

}

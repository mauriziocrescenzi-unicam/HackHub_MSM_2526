package it.unicam.cs.service;

import it.unicam.cs.model.Account;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.repository.AccountRepository;
import org.springframework.stereotype.Service;

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

}

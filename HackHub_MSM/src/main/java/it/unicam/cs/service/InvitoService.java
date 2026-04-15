package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.InvitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestisce la logica di invio, accettazione e rifiuto degli inviti a partecipare a un team.
 */
@Service
@Transactional
public class InvitoService {

    private final InvitoRepository repository;
    private final TeamHackathonService teamHackathonService;
    private final TeamService teamService;
    private final AccountService accountService;
    private final MembroTeamService membroTeamService;
    private final HackathonService hackathonService;

    public InvitoService(InvitoRepository repository, TeamHackathonService teamHackathonService, TeamService teamService, AccountService accountService,
                          MembroTeamService membroTeamService, HackathonService hackathonService) {
        this.repository = repository;
        this.teamHackathonService = teamHackathonService;
        this.teamService = teamService;
        this.accountService = accountService;
        this.membroTeamService = membroTeamService;
        this.hackathonService = hackathonService;
    }


    /**
     * Invia un invito da un mittente a un destinatario
     */
    public Invito inviareInvito(Account mittente, Account destinatario) {

        Team teamMittente = getTeamByAccount(mittente);
        Hackathon hackathon = teamHackathonService.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);

        if (hackathon == null ||
                hackathonService.getStatoHackathon(hackathon) != StatoHackathon.IN_ISCRIZIONE)
            throw new IllegalArgumentException("Hackathon non in stato di iscrizione.");
        if (!accountService.isPresent(destinatario.getEmail()))
            throw new IllegalArgumentException("Utente non esistente.");
        if (!teamService.verificaDisponibilitaMembro(destinatario))
            throw new IllegalArgumentException("Utente occupato.");
        if (checkDuplicateInviti(mittente.getId(), destinatario.getId()))
            throw new IllegalArgumentException("Invito esistente.");

        Invito nuovoInvito = new Invito(mittente.getId().intValue(), destinatario.getId().intValue());
        repository.save(nuovoInvito);
        return nuovoInvito;
    }

    /**
     * Restituisce il team di cui l'utente è membro
     */
    private Team getTeamByAccount(Account account) {
        MembroTeam membro = membroTeamService.getMembro(account);
        if (membro == null)
            throw new IllegalArgumentException("L'utente non è membro di nessun team.");
        return teamService.getTeamById(membro.getTeam().getId());
    }

    /**
     * Restituisce la lista degli inviti in attesa per un dato utente destinatario
     */
    public List<Invito> getInviti(Account account) {
        if (account == null) throw new IllegalArgumentException();
        return repository.findAll().stream()
                .filter(i -> i.getIdUtenteDestinatario() == account.getId().intValue()
                        && i.getStato() == StatoInvito.IN_ATTESA)
                .collect(Collectors.toList());
    }

    /**
     *  Controlla se esiste già un invito in attesa tra il mittente e il destinatario.
     */
    public boolean checkDuplicateInviti(Long idMittente, Long idDestinatario) {
        return repository.findAll().stream()
                .anyMatch(i -> i.getIdUtenteMittente() == idMittente
                        && i.getIdUtenteDestinatario() == idDestinatario
                        && i.getStato() == StatoInvito.IN_ATTESA);
    }

    /**
     * Valuta la risposta dell'utente all'invito, accettando o rifiutando
     */
    public void valutareInvito(Invito invito, Account utenteRichiedente, boolean risposta) {
        if (invito == null || utenteRichiedente == null)
            throw new IllegalArgumentException();
        if (invito.getIdUtenteDestinatario() != utenteRichiedente.getId().intValue())
            throw new IllegalArgumentException("L'invito non è destinato a questo utente.");

        if (risposta)
            accettaInvito(invito, utenteRichiedente);
        else
            rifiutaInvito(invito);
    }
    /**
     *  Accetta l'invito, aggiunge l'utente al team e aggiorna il database.
     */
    private void accettaInvito(Invito invito, Account destinatario) {
        if (accountService.isMembroTeam(destinatario))
            throw new IllegalArgumentException("Utente già membro di un team.");

        Account mittente = accountService.findById((long) invito.getIdUtenteMittente());
        Team teamMittente = getTeamByAccount(mittente);
        Hackathon hackathon = teamHackathonService.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);

        int maxMembri = (hackathon != null)
                ? hackathon.getDimensioneMassimoTeam()
                : Integer.MAX_VALUE;

        if (membroTeamService.getMembri(teamMittente.getId()).size() >= maxMembri)
            throw new IllegalArgumentException("Team completo.");

        membroTeamService.addMembro(destinatario.getId(), teamMittente.getId());
        invito.accettare();
        repository.save(invito);
    }

    /**
     *  Rifiuta l'invito e aggiorna il database.
     */
    private void rifiutaInvito(Invito invito) {
        invito.rifiutare();
        repository.save(invito);
    }
}
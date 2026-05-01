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
    /**
     * Costruisce un'istanza di {@code InvitoService} con le dipendenze necessarie.
     *
     * @param repository           repository per l'accesso agli inviti
     * @param teamHackathonService service per la gestione delle iscrizioni team-hackathon
     * @param teamService          service per la gestione dei team
     * @param accountService       service per la gestione degli account
     * @param membroTeamService    service per la gestione dei membri del team
     * @param hackathonService     service per la gestione degli hackathon
     */
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
     * Invia un invito da un mittente a un destinatario per unirsi al suo team.
     * Verifica che l'hackathon sia in stato di iscrizione, che il destinatario esista,
     * che sia disponibile e che non esista già un invito pendente tra i due utenti.
     *
     * @param mittente     l'account che invia l'invito; deve essere membro di un team
     * @param destinatario l'account che riceve l'invito
     * @return l'invito creato e salvato
     * @throws IllegalArgumentException se l'hackathon non è in iscrizione, il destinatario
     *                                  non esiste, è già in un team, o esiste già un invito pendente
     */
    public Invito inviareInvito(Account mittente, Account destinatario) {

        Team teamMittente = getTeamByAccount(mittente);
        Hackathon hackathon = teamHackathonService.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);
        if (hackathon == null ||
                (hackathonService.getStatoHackathon(hackathon) != StatoHackathon.IN_ISCRIZIONE &&
                        hackathonService.getStatoHackathon(hackathon) != StatoHackathon.CONCLUSO))
            throw new IllegalArgumentException("Hackathon non in stato valido per inviare inviti.");
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
     * Restituisce il team di cui l'account specificato è membro.
     *
     * @param account l'account di cui recuperare il team
     * @return il team associato all'account
     * @throws IllegalArgumentException se l'utente non è membro di nessun team
     */
    private Team getTeamByAccount(Account account) {
        MembroTeam membro = membroTeamService.getMembro(account);
        if (membro == null)
            throw new IllegalArgumentException("L'utente non è membro di nessun team.");
        return teamService.getTeamById(membro.getTeam().getId());
    }

    /**
     * Restituisce la lista degli inviti in attesa per un dato utente destinatario.
     *
     * @param account l'account destinatario; non può essere {@code null}
     * @return lista degli inviti con stato {@link StatoInvito#IN_ATTESA} destinati all'utente
     * @throws IllegalArgumentException se {@code account} è {@code null}
     */
    public List<Invito> getInviti(Account account) {
        if (account == null) throw new IllegalArgumentException();
        return repository.findAll().stream()
                .filter(i -> i.getIdUtenteDestinatario() == account.getId().intValue()
                        && i.getStato() == StatoInvito.IN_ATTESA)
                .collect(Collectors.toList());
    }

    /**
     * Controlla se esiste già un invito in attesa tra il mittente e il destinatario.
     *
     * @param idMittente     ID dell'utente mittente
     * @param idDestinatario ID dell'utente destinatario
     * @return {@code true} se esiste già un invito pendente, {@code false} altrimenti
     */
    public boolean checkDuplicateInviti(Long idMittente, Long idDestinatario) {
        return repository.findAll().stream()
                .anyMatch(i -> i.getIdUtenteMittente() == idMittente
                        && i.getIdUtenteDestinatario() == idDestinatario
                        && i.getStato() == StatoInvito.IN_ATTESA);
    }

    /**
     * Valuta la risposta dell'utente a un invito ricevuto, accettandolo o rifiutandolo.
     *
     * @param invito            l'invito da valutare; non può essere {@code null}
     * @param utenteRichiedente l'account che risponde; deve corrispondere al destinatario dell'invito
     * @param risposta          {@code true} per accettare, {@code false} per rifiutare
     * @throws IllegalArgumentException se {@code invito} o {@code utenteRichiedente} sono {@code null},
     *                                  oppure se l'invito non è destinato all'utente richiedente
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
     * Accetta l'invito, aggiunge il destinatario al team del mittente e aggiorna il database.
     * Verifica che il destinatario non sia già in un team e che il team non sia al completo.
     *
     * @param invito      l'invito da accettare
     * @param destinatario l'account che accetta l'invito
     * @throws IllegalArgumentException se il destinatario è già membro di un team
     *                                  o se il team ha raggiunto la dimensione massima consentita
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
     * Rifiuta l'invito e aggiorna il suo stato nel database.
     *
     * @param invito l'invito da rifiutare
     */
    private void rifiutaInvito(Invito invito) {
        invito.rifiutare();
        repository.save(invito);
    }
}
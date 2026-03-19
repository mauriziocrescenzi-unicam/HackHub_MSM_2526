package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestisce la logica di invio, accettazione e rifiuto degli inviti a partecipare a un team.
 */
public class InvitoService {

    private static InvitoService instance;

    private final StandardPersistence<Invito> persistence;
    private final TeamService teamService;
    private final UtenteService utenteService;
    private final MembroTeamService membroTeamService;
    private final HackathonService hackathonService;

    private InvitoService() {
        persistence = new StandardPersistence<>(Invito.class);
        teamService = TeamService.getInstance();
        utenteService = UtenteService.getInstance();
        membroTeamService = MembroTeamService.getInstance();
        hackathonService = HackathonService.getInstance();
    }

    public static InvitoService getInstance() {
        if (instance == null)
            instance = new InvitoService();
        return instance;
    }

    /**
     * Invia un invito da un mittente a un destinatario
     */
    public Invito inviareInvito(Utente mittente, Utente destinatario) {

        Team teamMittente = getTeamByUtente(mittente);
        Hackathon hackathon = teamService.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);

        if (hackathon == null ||
                hackathonService.getStatoHackathon(hackathon) != StatoHackathon.IN_ISCRIZIONE)
            throw new IllegalArgumentException("Hackathon non in stato di iscrizione.");
        if (!utenteService.isPresent(destinatario))
            throw new IllegalArgumentException("Utente non esistente.");
        if (!teamService.verificaDisponibilitaMembro(destinatario))
            throw new IllegalArgumentException("Utente occupato.");
        if (checkDuplicateInviti(mittente.getId().intValue(), destinatario.getId().intValue()))
            throw new IllegalArgumentException("Invito esistente.");

        Invito nuovoInvito = new Invito(mittente.getId().intValue(), destinatario.getId().intValue());
        persistence.create(nuovoInvito);
        return nuovoInvito;
    }

    /**
     * Restituisce il team di cui l'utente è membro
     */
    private Team getTeamByUtente(Utente utente) {
        MembroTeam membro = membroTeamService.getMembro(utente);
        if (membro == null)
            throw new IllegalArgumentException("L'utente non è membro di nessun team.");
        return teamService.getTeamById(membro.getTeam().getId());
    }

    /**
     * Restituisce la lista degli inviti in attesa per un dato utente destinatario
     */
    public List<Invito> getInviti(Utente utente) {
        if (utente == null) throw new IllegalArgumentException();
        return persistence.getAll().stream()
                .filter(i -> i.getIdUtenteDestinatario() == utente.getId().intValue()
                        && i.getStato() == StatoInvito.IN_ATTESA)
                .collect(Collectors.toList());
    }

    /**
     *  Controlla se esiste già un invito in attesa tra il mittente e il destinatario.
     */
    public boolean checkDuplicateInviti(int idUtenteMittente, int idUtenteDestinatario) {
        return persistence.getAll().stream()
                .anyMatch(i -> i.getIdUtenteMittente() == idUtenteMittente
                        && i.getIdUtenteDestinatario() == idUtenteDestinatario
                        && i.getStato() == StatoInvito.IN_ATTESA);
    }

    /**
     * Valuta la risposta dell'utente all'invito, accettando o rifiutando
     */
    public void valutareInvito(Invito invito, Utente utenteRichiedente, boolean risposta) {
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
    private void accettaInvito(Invito invito, Utente destinatario) {
        if (utenteService.isMembroTeam(destinatario))
            throw new IllegalArgumentException("Utente già membro di un team.");

        Utente mittente = utenteService.findById((long) invito.getIdUtenteMittente());
        Team teamMittente = getTeamByUtente(mittente);
        Hackathon hackathon = teamService.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);

        int maxMembri = (hackathon != null)
                ? hackathon.getDimensioneMassimoTeam()
                : Integer.MAX_VALUE;

        if (membroTeamService.getMembri(teamMittente.getId()).size() >= maxMembri)
            throw new IllegalArgumentException("Team completo.");

        membroTeamService.addMembro(destinatario.getId(), teamMittente.getId());
        invito.accettare();
        persistence.update(invito);
    }

    /**
     *  Rifiuta l'invito e aggiorna il database.
     */
    private void rifiutaInvito(Invito invito) {
        invito.rifiutare();
        persistence.update(invito);
    }
}
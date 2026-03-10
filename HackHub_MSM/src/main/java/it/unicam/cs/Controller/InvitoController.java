package it.unicam.cs.Controller;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestisce la logica di invio, accettazione e rifiuto degli inviti a partecipare a un team.
 */
public class InvitoController {

    private static InvitoController instance;

    private final StandardPersistence<Invito> persistence;
    private final TeamController teamController;
    private final UtenteController utenteController;
    private final MembroTeamController membroTeamController;
    private final HackathonController hackathonController;

    private InvitoController(TeamController teamController, UtenteController utenteController,
                             HackathonController hackathonController) {
        this.persistence = new StandardPersistence<>(Invito.class);
        this.teamController = teamController;
        this.utenteController = utenteController;
        this.membroTeamController = teamController.getMembroTeamController();
        this.hackathonController = hackathonController;
    }

    public static InvitoController getInstance(TeamController teamController,
                                               UtenteController utenteController,
                                               HackathonController hackathonController) {
        if (instance == null)
            instance = new InvitoController(teamController, utenteController, hackathonController);
        return instance;
    }

    /**
     * Invia un invito da un mittente a un destinatario
     */
    public Invito inviareInvito(Utente mittente, Utente destinatario) {

        Team teamMittente = getTeamByUtente(mittente);
        Hackathon hackathon = teamController.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);

        if (hackathon == null ||
                hackathonController.getStatoHackathon(hackathon) != StatoHackathon.IN_INISCRIZIONE)
            throw new IllegalArgumentException("Hackathon non in stato di iscrizione.");
        if (!utenteController.isPresent(destinatario))
            throw new IllegalArgumentException("Utente non esistente.");
        if (!teamController.verificaDisponibilitaMembro(destinatario))
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
        MembroTeam membro = membroTeamController.getMembro(utente);
        if (membro == null)
            throw new IllegalArgumentException("L'utente non è membro di nessun team.");
        return teamController.getTeamById(membro.getIdTeam());
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
        if (utenteController.isMembroTeam(destinatario))
            throw new IllegalArgumentException("Utente già membro di un team.");

        Utente mittente = utenteController.findById((long) invito.getIdUtenteMittente());
        Team teamMittente = getTeamByUtente(mittente);
        Hackathon hackathon = teamController.getHackathon(teamMittente)
                .stream().findFirst().orElse(null);

        int maxMembri = (hackathon != null)
                ? hackathon.getDimensioneMassimoTeam()
                : Integer.MAX_VALUE;

        if (membroTeamController.getMembri(teamMittente.getId()).size() >= maxMembri)
            throw new IllegalArgumentException("Team completo.");

        membroTeamController.addMembro(destinatario.getId().intValue(), teamMittente.getId());
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
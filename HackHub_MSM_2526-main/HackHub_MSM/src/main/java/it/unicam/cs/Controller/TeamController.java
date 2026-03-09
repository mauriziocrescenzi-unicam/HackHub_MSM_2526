package it.unicam.cs.Controller;

import it.unicam.cs.model.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeamController implements TeamInterface {
    private List<Team> teamList;
    private List<TeamHackathon> teamHackathonList;
    private MembroTeamController membroTeamController;
    private HackathonController hackathonController;
    private int nextTeamId;
    private int nextTeamHackathonId;

    public TeamController(HackathonController hackathonController) {
        this.teamList = new ArrayList<>();
        this.teamHackathonList = new ArrayList<>();
        this.membroTeamController = new MembroTeamController();
        this.hackathonController = hackathonController;
        this.nextTeamId = 1;
        this.nextTeamHackathonId = 1;
    }

    // Metodi dal diagramma di progetto
    @Override
    public boolean creaTeam() {
        // Questo metodo dovrebbe essere chiamato con parametri
        // Implementazione base - da completare con UI
        return false;
    }

    public boolean creaTeam(String nome, String descrizione, int idUtenteCreatore) {
        // Verifica che l'utente non sia già membro di un team
        Utente utenteCreatore = new Utente(idUtenteCreatore);
        if (membroTeamController.isMembroTeam(utenteCreatore)) {
            return false;
        }

        // Crea il team
        Team nuovoTeam = new Team(nextTeamId++, nome, descrizione, idUtenteCreatore);
        teamList.add(nuovoTeam);

        // Aggiungi il creatore come amministratore/membro
        membroTeamController.addMembro(idUtenteCreatore, nuovoTeam.getId());

        return true;
    }

    @Override
    public List<Hackathon> getHackathonDisponibili() {
        List<Hackathon> disponibili = new ArrayList<>();
        if (hackathonController != null) {
            List<Hackathon> tutti = hackathonController.getListaHackathon();
            
            for (Hackathon h : tutti) {
                if (h.getStato() == StatoHackathon.IN_ISCRIZIONE) {
                    disponibili.add(h);
                }
            }
        }
        return disponibili;
    }

    @Override
    public boolean iscrivereTeam(Hackathon hackathon, Team team) {
        return iscriviHackathon(team, hackathon);
    }

    public boolean iscriviHackathon(Team team, Hackathon hackathon) {
        if (team == null || hackathon == null) {
            return false;
        }

        // Verifica che il team non sia già iscritto
        if (team.iscritto(hackathon)) {
            return false; // Team già iscritto
        }

        // Verifica scadenza iscrizioni
        if (hackathon.getDataScadenzaIscrizioni().isBefore(LocalDateTime.now())) {
            return false; // Iscrizione scaduta
        }

        // Verifica requisiti dimensione team
        int maxMembri = hackathon.getDimensioneMassimaTeam();
        if (team.getMembri() > maxMembri) {
            return false; // Team troppo numeroso
        }

        // Verifica requisiti minimi (almeno 1 membro)
        if (team.getMembri() < 1) {
            return false; // Team senza membri
        }

        // Crea associazione TeamHackathon
        TeamHackathon teamHackathon = new TeamHackathon(
            nextTeamHackathonId++, 
            team, 
            hackathon
        );
        
        teamHackathonList.add(teamHackathon);
        team.aggiungiHackathon(teamHackathon);

        return true;
    }

    public boolean iscrittoHackathon(Team team) {
        if (team == null) return false;
        return !team.getHackathonIscritti().isEmpty();
    }

    public boolean verificaDisponibilitaMembro(Utente utente) {
        if (utente == null) return false;
        return !membroTeamController.isMembroTeam(utente);
    }

    public boolean checkDuplicateInviti(Utente utente) {
        // Logica per verificare inviti duplicati (da implementare con InvitoController)
        return false;
    }

    public List<Hackathon> getHackathon(Team team) {
        List<Hackathon> result = new ArrayList<>();
        if (team == null || hackathonController == null) {
            return result;
        }
        
        for (TeamHackathon th : teamHackathonList) {
            if (th.getIdTeam() == team.getId()) {
                Hackathon h = hackathonController.getHackathonById(th.getIdHackathon());
                if (h != null) {
                    result.add(h);
                }
            }
        }
        return result;
    }

    public Team getTeamById(int idTeam) {
        for (Team team : teamList) {
            if (team.getId() == idTeam) {
                return team;
            }
        }
        return null;
    }

    public MembroTeamController getMembroTeamController() {
        return membroTeamController;
    }

    @Override
    public String toString() {
        return "TeamController{" +
                "numeroTeam=" + teamList.size() +
                ", numeroIscrizioni=" + teamHackathonList.size() +
                '}';
    }
}
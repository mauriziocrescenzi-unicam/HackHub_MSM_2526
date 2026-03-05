package it.unicam.cs.Controller;

import it.unicam.cs.model.MembroTeam;
import it.unicam.cs.model.Utente;
import java.util.ArrayList;
import java.util.List;

public class MembroTeamController {
    private List<MembroTeam> membriTeam;

    public MembroTeamController() {
        this.membriTeam = new ArrayList<>();
    }

    // Metodi dal diagramma di progetto
    public boolean isMembroTeam(Utente utente) {
        if (utente == null) return false;
        for (MembroTeam membro : membriTeam) {
            if (membro.getIdUtente() == utente.getId()) {
                return true;
            }
        }
        return false;
    }

    public List<MembroTeam> getMembri(int idTeam) {
        List<MembroTeam> result = new ArrayList<>();
        for (MembroTeam membro : membriTeam) {
            if (membro.getIdTeam() == idTeam) {
                result.add(membro);
            }
        }
        return result;
    }

    public boolean addMembro(int idUtente, int idTeam) {
        if (isMembroTeamById(idUtente)) {
            return false; // Utente già membro di un team
        }
        int nuovoId = membriTeam.size() + 1;
        MembroTeam nuovoMembro = new MembroTeam(nuovoId, idTeam, idUtente, "MEMBRO");
        membriTeam.add(nuovoMembro);
        return true;
    }

    // Metodo helper per verifica per ID
    private boolean isMembroTeamById(int idUtente) {
        for (MembroTeam membro : membriTeam) {
            if (membro.getIdUtente() == idUtente) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MembroTeamController{" +
                "numeroMembriTotali=" + membriTeam.size() +
                '}';
    }
}
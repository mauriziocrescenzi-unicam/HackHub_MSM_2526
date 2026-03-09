package it.unicam.cs.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Classe che rappresenta la chiave primaria composta per l'entità MembroTeam.
 * La chiave è formata dall'ID dell'Utente e dall'ID del Team.
 * Deve essere @Embeddable per essere incorporata nell'entità principale.
 */
@Embeddable
public class MembroTeamId implements Serializable {

    private Long utenteId;
    private Long teamId;

    public MembroTeamId() {}

    public MembroTeamId(Long utenteId, Long teamId) {
        this.utenteId = utenteId;
        this.teamId = teamId;
    }

    // Getters generati automaticamente da Lombok @Getter sulla classe (se configurato)
    // o specifici se necessari
    public Long getUtenteId() { 
        return utenteId; 
    }

    public void setUtenteId(Long utenteId) { 
        this.utenteId = utenteId; 
    }
    
    public Long getTeamId() { 
        return teamId; 
    }
    
    public void setTeamId(Long teamId) { 
        this.teamId = teamId; 
    }

    /**
     * Verifica l'uguaglianza tra due chiavi composte.
     * Confronta sia utenteId che teamId per determinare se due istanze rappresentano la stessa relazione.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembroTeamId that = (MembroTeamId) o;
        return Objects.equals(utenteId, that.utenteId) &&
               Objects.equals(teamId, that.teamId);
    }

    /**
     * Calcola l'hash code basato sui campi della chiave composta.
     * Necessario per il corretto funzionamento nelle collezioni hash-based.
     */
    @Override
    public int hashCode() {
        return Objects.hash(utenteId, teamId);
    }
}
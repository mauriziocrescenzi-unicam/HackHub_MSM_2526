package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.HackathonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsabile della gestione dei membri dello staff nel sistema HackHub.
 * Implementa il pattern Singleton.
 */
@Service
@Transactional
public class MembroDelloStaffService {

    private final SottomissioneService sottomissioneService;
    private  final HackathonRepository hackathonRepository;
    public MembroDelloStaffService(SottomissioneService sottomissioneService,HackathonRepository hackathonRepository) {
        this.sottomissioneService = sottomissioneService;
        this.hackathonRepository = hackathonRepository;
    }

    /**
     * Restituisce la lista degli hackathon a cui è assegnato il membro dello staff.
     *
     * @param membro Il membro dello staff di cui recuperare gli hackathon
     * @return Lista di hackathon a cui il membro è assegnato in qualsiasi ruolo
     */
    public List<Hackathon> getListaHackathon(MembroDelloStaff membro) {
        if (membro == null) throw new IllegalArgumentException("Membro dello staff non valido.");
        List<Hackathon> tutti = hackathonRepository.findAll();
        List<Hackathon> risultato = new ArrayList<>();

        for (Hackathon h : tutti) {
            if (isMembroAssegnato(h, membro)) {
                risultato.add(h);
            }
        }
        return risultato;
    }
    /**
     * Restituisce la lista delle sottomissioni per un hackathon selezionato.
     *
     * @param hackathon L'hackathon selezionato dal membro dello staff
     * @return Lista di sottomissioni dell'hackathon
     */
    public List<Sottomissione> getSottomissioni(Hackathon hackathon) {
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non valido.");
        return sottomissioneService.getSottomissioni(hackathon);
    }

    /**
     * Restituisce una sottomissione specifica per ID.
     *
     * @param idSottomissione L'ID della sottomissione da recuperare
     * @return La sottomissione corrispondente all'ID
     */
    public Sottomissione getSottomissione(Long idSottomissione) {
        if (idSottomissione == null) throw new IllegalArgumentException("Id non valido.");
        return sottomissioneService.getSottomissioneById(idSottomissione);
    }

    /**
     * Verifica se un membro dello staff è assegnato a un hackathon
     * in uno qualsiasi dei tre ruoli possibili: giudice, organizzatore, mentore.
     *
     * @param hackathon L'hackathon da verificare
     * @param membro    Il membro dello staff
     * @return true se è assegnato in qualsiasi ruolo, false altrimenti
     */
    private boolean isMembroAssegnato(Hackathon hackathon, MembroDelloStaff membro) {
        if (hackathon.getGiudice() != null
                && hackathon.getGiudice().getId().equals(membro.getId())) {
            return true;
        }
        if (hackathon.getOrganizzatore() != null
                && hackathon.getOrganizzatore().getId().equals(membro.getId())) {
            return true;
        }
        if (hackathon.getMentori() != null) {
            for (Mentore m : hackathon.getMentori()) {
                if (m.getId().equals(membro.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
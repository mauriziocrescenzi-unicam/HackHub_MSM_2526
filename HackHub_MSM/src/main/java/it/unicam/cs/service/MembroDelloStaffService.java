package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MembroDelloStaffService {

    private final SottomissioneService sottomissioneService;
    private final HackathonRepository hackathonRepository;

    public MembroDelloStaffService(SottomissioneService sottomissioneService,
                                   HackathonRepository hackathonRepository) {
        this.sottomissioneService = sottomissioneService;
        this.hackathonRepository = hackathonRepository;
    }

    /**
     * Restituisce la lista degli hackathon a cui è assegnato un membro dello staff.
     *
     * @param idMembro ID del membro dello staff
     * @return Lista di hackathon a cui il membro è assegnato, o lista vuota se non trovato
     */
    public List<Hackathon> getListaHackathon(Long idMembro) {
        if (idMembro == null || idMembro <= 0) {
            throw new IllegalArgumentException("ID del membro dello staff non valido.");
        }
        List<Hackathon> risultato = new ArrayList<>();
        // Cerca hackathon dove è organizzatore
        risultato.addAll(hackathonRepository.findByOrganizzatoreId(idMembro));
        // Cerca hackathon dove è giudice
        risultato.addAll(hackathonRepository.findByGiudiceId(idMembro));
        // Cerca hackathon dove è mentore
        risultato.addAll(hackathonRepository.findByMentoriId(idMembro));
        // Rimuovi duplicati (nel caso raro in cui un membro abbia più ruoli nello stesso hackathon)
        return risultato.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Restituisce la lista delle sottomissioni per un hackathon selezionato.
     */
    public List<Sottomissione> getSottomissioni(Hackathon hackathon) {
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non valido.");
        return sottomissioneService.getSottomissioni(hackathon);
    }

    /**
     * Restituisce una sottomissione specifica per ID.
     */
    public Sottomissione getSottomissione(Long idSottomissione) {
        if (idSottomissione == null) throw new IllegalArgumentException("Id non valido.");
        return sottomissioneService.getSottomissioneById(idSottomissione);
    }
}
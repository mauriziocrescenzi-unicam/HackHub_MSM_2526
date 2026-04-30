package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service per la gestione dei membri dello staff nel sistema HackHub.
 * Fornisce operazioni per recuperare gli hackathon associati a un membro dello staff,
 * sia in base al ruolo (organizzatore, giudice, mentore) che allo stato dell'hackathon.
 */
@Service
@Transactional
public class MembroDelloStaffService {

    private final HackathonRepository hackathonRepository;
    /**
     * Costruisce un'istanza di {@code MembroDelloStaffService} con le dipendenze necessarie.
     *
     * @param hackathonRepository  repository per l'accesso agli hackathon
     */
    public MembroDelloStaffService(HackathonRepository hackathonRepository) {
        this.hackathonRepository = hackathonRepository;
    }

    //TODO cambio nome metodo
    /**
     * Restituisce la lista degli hackathon di un determinato organizzatore in base allo stato
     * @param stato stato degli hackathon
     * @param id id del membro dello staff
     * @return lista degli hackathon
     */
    public List<Hackathon> getListaHackathons(StatoHackathon stato,long id){
        if(stato == null) throw new NullPointerException("Stato dell'hackathon non valido");
        if(id < 0) throw new IllegalArgumentException("Id dell'organizzatore non valido");
        return hackathonRepository.findAll().stream()
                .filter(h -> h.getOrganizzatore().getId() == id && h.getStato() == stato)
                .toList();
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


}
package it.unicam.cs.service;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.AccountRepository;
import it.unicam.cs.repository.HackathonRepository;
import it.unicam.cs.repository.SegnalazioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizzatoreService {
    private final AccountRepository repository;
    private final HackathonRepository hackathonRepository;
    private final SegnalazioneRepository segnalazioneRepository;
    public OrganizzatoreService(AccountRepository repository, HackathonRepository hackathonRepository, SegnalazioneRepository segnalazioneRepository) {
        this.repository = repository;
        this.hackathonRepository = hackathonRepository;
        this.segnalazioneRepository = segnalazioneRepository;
    }


    /**
     * Restituisce la lista degli hackathon di un determinato organizzatore in base allo stato
     * @param stato stato degli hackathon
     * @param idOrganizzatore id dell'organizzatore
     * @return lista degli hackathon
     */
    public List<Hackathon> getListaHackathons(StatoHackathon stato,long idOrganizzatore){
            if(stato == null) throw new NullPointerException("Stato dell'hackathon non valido");
            if(idOrganizzatore < 0) throw new IllegalArgumentException("Id dell'organizzatore non valido");
            return hackathonRepository.findAll().stream()
                    .filter(h -> h.getOrganizzatore().getId() == idOrganizzatore && h.getStato() == stato)
                    .toList();
    }

    /**
     * Restituisce la lista delle segnalazioni di un determinato organizzatore
     * @param organizzatore organizzatore che ha segnalato
     * @param hackathon hackathon relativo alle segnalazioni
     * @param stato stato delle segnalazioni
     * @return lista delle segnalazioni
     */
    public List<Segnalazione> getSegnalazioni(Account organizzatore, List<Hackathon> hackathon, StatoSegnalazione stato){
        if (organizzatore == null)throw new NullPointerException("Organizzatore non valido");
        if (hackathon==null || hackathon.isEmpty()) throw new IllegalArgumentException("Hackathon non valido");
        if (stato==null) throw new NullPointerException("Stato non valido");
        return segnalazioneRepository.findAll().stream().filter(s -> hackathon.contains(s.getHackathon())
                && s.getHackathon().getOrganizzatore().equals(organizzatore) && s.getStato() == stato).toList();


    }
    public Account getOrganizzatoreById(long idOrganizzatore) {
        return repository.findById(idOrganizzatore).orElse(null);
    }
}

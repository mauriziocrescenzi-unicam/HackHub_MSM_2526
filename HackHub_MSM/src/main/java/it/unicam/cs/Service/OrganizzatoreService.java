package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.util.List;

public class OrganizzatoreService {
    private static OrganizzatoreService service;
    StandardPersistence<Hackathon> hackathonPersistence;
    StandardPersistence<Segnalazione> segnalazionePersistence;
    private OrganizzatoreService(){
        hackathonPersistence = new StandardPersistence<>(Hackathon.class);
        segnalazionePersistence = new StandardPersistence<>(Segnalazione.class);
    }
    public static OrganizzatoreService getInstance(){
        if(service == null)
            service = new OrganizzatoreService();
        return service;
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
            return hackathonPersistence.getAll().stream()
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
    public List<Segnalazione> getSegnalazioni(Organizzatore organizzatore, List<Hackathon> hackathon, StatoSegnalazione stato){
        if (organizzatore == null)throw new NullPointerException("Organizzatore non valido");
        if (hackathon==null || hackathon.isEmpty()) throw new IllegalArgumentException("Hackathon non valido");
        if (stato==null) throw new NullPointerException("Stato non valido");
        return segnalazionePersistence.getAll().stream().filter(s -> hackathon.contains(s.getHackathon())
                && s.getHackathon().getOrganizzatore().equals(organizzatore) && s.getStato() == stato).toList();


    }
}

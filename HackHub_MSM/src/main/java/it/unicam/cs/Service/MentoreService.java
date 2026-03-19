package it.unicam.cs.Service;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Mentore;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.persistence.StandardPersistence;

import java.util.List;
import java.util.Objects;

public class MentoreService {
    private static MentoreService service;
    private final StandardPersistence<Mentore> persistence;
    private final StandardPersistence<Hackathon> hackathonPersistence;
    private MentoreService(){
        persistence = new StandardPersistence<>(Mentore.class);
        hackathonPersistence = new StandardPersistence<>(Hackathon.class);
    }

    public static MentoreService getInstance(){
        if(service == null)
            service = new MentoreService();
        return service;
    }

    public static boolean assegnaMentore(Hackathon hackathon, List<Mentore> mentori){
        if(hackathon ==null || mentori.isEmpty()) throw new NullPointerException();
        hackathon.setMentori(mentori);
        return true;
    }
    public static boolean verificaMentore(List<Mentore> mentori){
        if (mentori == null || mentori.isEmpty())
            throw new IllegalArgumentException("La lista dei mentori non può essere nulla o vuota");

        if (mentori.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("La lista non può contenere mentori nulli");

        if (mentori.stream().map(Mentore::getId).distinct().count() != mentori.size())
            throw new IllegalArgumentException("La lista contiene mentori duplicati");

        return true;
    }
    public List<Mentore> getListaMentori(){
        return persistence.getAll();
    }

    /**
     * Aggiunge più mentori ad un hackathon
     * @param mentori lista di mentori
     * @param hackathon hackathon a cui aggiungere i mentori
     * @return true se l'aggiunta
     */
    public boolean aggiungiMentori(List<Mentore> mentori,Hackathon hackathon){
        if(hackathon == null) throw new NullPointerException("Hackathon non valido");
        if (mentori == null) throw new NullPointerException("La lista dei mentori non può essere nulla");
        verificaMentore(mentori);
        hackathon.setMentori(mentori);
        hackathonPersistence.update(hackathon);
        return true;
    }

    /**
     * Restituisce tutti gli hackathon di un determinato mentore in base allo stato
     * @param stato stato degli hackathon
     * @param idMentore id del mentore
     * @return lista degli hackathon
     */
    public List<Hackathon> getListaHackathons(StatoHackathon stato, long idMentore){
        if(stato == null) throw new NullPointerException("Stato dell'hackathon non valido");
        if(idMentore < 0) throw new IllegalArgumentException("Id  del mentore non valido");
        return hackathonPersistence.getAll().stream()
                .filter(h -> h.getOrganizzatore().getId() == idMentore && h.getStato() == stato)
                .toList();
    }

    public Mentore getMentoreById(long idMentore) {
        return persistence.findById(idMentore);
    }
}

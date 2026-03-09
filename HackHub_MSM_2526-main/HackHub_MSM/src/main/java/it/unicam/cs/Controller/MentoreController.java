package it.unicam.cs.Controller;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Mentore;
import it.unicam.cs.persistence.StandardPersistence;

import java.util.List;

public class MentoreController {
    private static MentoreController controller;
    private final StandardPersistence<Mentore> persistence;
    private MentoreController(){
        persistence = new StandardPersistence<>(Mentore.class);
    }

    public static MentoreController getInstance(){
        if(controller == null)
            controller = new MentoreController();
        return controller;
    }

    public static boolean assegnaMentore(Hackathon hackathon, List<Mentore> mentori){
        if(hackathon ==null || mentori.isEmpty()) throw new NullPointerException();
        hackathon.setMentori(mentori);
        return true;
    }
    public static boolean verificaMentore(){return false;}
    public List<Mentore> getListaMentori(){
        return persistence.getAll();
    }
}

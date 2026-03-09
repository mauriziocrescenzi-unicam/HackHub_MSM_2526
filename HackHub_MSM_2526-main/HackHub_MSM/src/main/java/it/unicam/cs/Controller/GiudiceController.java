package it.unicam.cs.Controller;

import it.unicam.cs.model.Giudice;
import it.unicam.cs.model.Hackathon;
import it.unicam.cs.persistence.StandardPersistence;

import java.util.List;

public class GiudiceController {
    private static GiudiceController controller;
    private final StandardPersistence<Giudice> persistence;


    private GiudiceController(){
        persistence = new StandardPersistence<>(Giudice.class);
    }
    public static GiudiceController getInstance(){
        if(controller == null) controller = new GiudiceController();
        return controller;
    }

    public List<Giudice> getListaGiudici(){
        return persistence.getAll();
    }

    public boolean verificaGiudice(Giudice giudice){
        return false;
    }
}

package it.unicam.cs.Service;

import it.unicam.cs.model.Giudice;
import it.unicam.cs.persistence.StandardPersistence;

import java.util.List;

public class GiudiceService {
    private static GiudiceService service;
    private final StandardPersistence<Giudice> persistence;


    private GiudiceService(){
        persistence = new StandardPersistence<>(Giudice.class);
    }
    public static GiudiceService getInstance(){
        if(service == null) service = new GiudiceService();
        return service;
    }

    public List<Giudice> getListaGiudici(){
        return persistence.getAll();
    }

    public boolean verificaGiudice(Giudice giudice){
        return false;
    }
}

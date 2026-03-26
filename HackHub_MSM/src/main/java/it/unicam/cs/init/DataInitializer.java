package it.unicam.cs.init;

import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UtenteRepository utenteRepository;
    private final OrganizzatoreRepository organizzatoreRepository;
    private final MentoreRepository mentoreRepository;
    private final GiudiceRepository giudiceRepository;
    public DataInitializer(UtenteRepository utenteRepository, OrganizzatoreRepository organizzatoreRepository, MentoreRepository mentoreRepository, GiudiceRepository giudiceRepository) {
        this.utenteRepository = utenteRepository;
        this.organizzatoreRepository = organizzatoreRepository;
        this.mentoreRepository = mentoreRepository;
        this.giudiceRepository = giudiceRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        Organizzatore organizzatore= new Organizzatore("o@test.it","org","org");
        Utente utente = new Utente("u@test.it", "user", "user");
        Mentore mentore = new Mentore("m@test.it", "mentore", "mentore");
        Giudice giudice = new Giudice("g@test.it", "giudice", "giudice");
        utenteRepository.save(utente);
        organizzatoreRepository.save(organizzatore);
        mentoreRepository.save(mentore);
        giudiceRepository.save(giudice);
    }
}

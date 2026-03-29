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

    public DataInitializer(UtenteRepository utenteRepository, OrganizzatoreRepository organizzatoreRepository,
                           MentoreRepository mentoreRepository, GiudiceRepository giudiceRepository) {
        this.utenteRepository = utenteRepository;
        this.organizzatoreRepository = organizzatoreRepository;
        this.mentoreRepository = mentoreRepository;
        this.giudiceRepository = giudiceRepository;
    }

    @Override
    public void run(String... args) {
        if (utenteRepository.count() == 0) {
            Utente utente = new Utente("mario@test.it", "Mario", "Rossi");
            Organizzatore organizzatore = new Organizzatore("luigi@test.it", "Luigi", "Rossi");
            Mentore mentore = new Mentore("anna@test.it", "Anna", "Rossi");
            Giudice giudice = new Giudice("carlo@test.it", "Carlo", "Rossi");
            Mentore mentore1 = new Mentore("mm@test.it","Mario","Marroni");

            utenteRepository.save(utente);
            organizzatoreRepository.save(organizzatore);
            mentoreRepository.save(mentore);
            giudiceRepository.save(giudice);
            mentoreRepository.save(mentore1);

            System.out.println("Utente ID: " + utente.getId());
            System.out.println("Organizzatore ID: " + organizzatore.getId());
            System.out.println("Mentore ID: " + mentore.getId());
            System.out.println("Giudice ID: " + giudice.getId());
        }
    }
}
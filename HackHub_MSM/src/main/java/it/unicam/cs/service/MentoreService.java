package it.unicam.cs.service;

import it.unicam.cs.model.Hackathon;
import it.unicam.cs.model.Mentore;
import it.unicam.cs.model.RichiestaSupporto;
import it.unicam.cs.model.StatoHackathon;
import it.unicam.cs.repository.HackathonRepository;
import it.unicam.cs.repository.MentoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MentoreService {
    private final MentoreRepository repository;
    private final HackathonRepository hackathonRepository;
    private final RichiestaSupportoService richiestaSupportoService;

    public MentoreService(MentoreRepository repository,
                          HackathonRepository hackathonRepository,
                          RichiestaSupportoService richiestaSupportoService) {
        this.repository = repository;
        this.hackathonRepository = hackathonRepository;
        this.richiestaSupportoService = richiestaSupportoService;
    }


    public  boolean assegnaMentore(Hackathon hackathon, List<Mentore> mentori){
        if(hackathon ==null || mentori.isEmpty()) throw new NullPointerException();
        verificaMentore(mentori);
        hackathon.setMentori(mentori);
        hackathonRepository.save(hackathon);
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
        return repository.findAll();
    }

    /**
     * Aggiunge più mentori ad un hackathon
     * @param mentori lista di mentori
     * @param hackathon hackathon a cui aggiungere i mentori
     * @return true se l'aggiunta
     */
    public boolean aggiungiMentori(List<Long> mentori,Hackathon hackathon){
        if(hackathon == null) throw new NullPointerException("Hackathon non valido");
        if (mentori == null) throw new NullPointerException("La lista dei mentori non può essere nulla");
        //prendo i mentori dalla lista
        List<Mentore> mentoriDaAggiungere = mentori.stream().map(this::getMentoreById).toList();
        //controllo che non ci siano mentori nulli
        if (mentoriDaAggiungere.stream().anyMatch(Objects::isNull)) return false;
        verificaMentore(mentoriDaAggiungere);
        //aggiungo i mentori al hackathon
        Set<Mentore> mentoriEsistenti = new HashSet<>(hackathon.getMentori());
        mentoriEsistenti.addAll(mentoriDaAggiungere);
        hackathon.setMentori(new ArrayList<>(mentoriEsistenti));
        hackathonRepository.save(hackathon);
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
        return hackathonRepository.findAll().stream()
                .filter(h -> h.getMentori().stream().anyMatch(m -> m.getId().equals(idMentore))
                        && h.getStato() == stato).toList();
    }

    public Mentore getMentoreById(long idMentore) {
        return repository.findById(idMentore).orElse(null);
    }

    public List<RichiestaSupporto> getRichiesteSupporto(Hackathon hackathon) {
        return richiestaSupportoService.getRichiesteSupporto(hackathon);
    }

    public RichiestaSupporto getRichiestaSupporto(Long idRichiesta) {
        return richiestaSupportoService.getRichiestaSupporto(idRichiesta);
    }

    /**
     * Verifica se una richiesta è già stata risolta.
     * Corrisponde a isRichiestaSupportoRisolta() nel sequence diagram.
     *
     * @param richiestaSupporto La richiesta da verificare
     * @return true se già risolta, false altrimenti
     */
    public boolean isRichiestaSupportoRisolta(RichiestaSupporto richiestaSupporto) {
        return richiestaSupportoService.isRichiestaSupportoRisolta(richiestaSupporto);
    }
    /**
     * Risponde a una richiesta di supporto.
     * Corrisponde a rispostaRichiestaSupporto(risposta) nel sequence diagram.
     * Delega la logica (checkRisposta + setRisposta) al RichiestaSupportoService.
     *
     * @param richiestaSupporto   La richiesta a cui rispondere
     * @param descrizioneRisposta Testo della risposta
     * @return true se la risposta è valida e salvata, false altrimenti
     */
    public boolean rispostaRichiestaSupporto(RichiestaSupporto richiestaSupporto,
                                             String descrizioneRisposta) {
        return richiestaSupportoService.rispostaRichiestaSupporto(richiestaSupporto, descrizioneRisposta);
    }
}

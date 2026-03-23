package it.unicam.cs.Service;

import it.unicam.cs.model.*;
import it.unicam.cs.persistence.StandardPersistence;

import java.time.LocalDateTime;
import java.util.List;

public class HackathonService {
    private static HackathonService service;
    private final GiudiceService giudiceService = GiudiceService.getInstance();
    private final Builder builder;
    private final StandardPersistence<Hackathon> persistence;

    public static HackathonService getInstance(){
        if(service == null) service = new HackathonService(new HackathonBuilder());
        return service;
    }
    HackathonService(Builder builder) {
        this.persistence = new StandardPersistence<>(Hackathon.class);
        this.builder = builder;
    }
    public boolean creaHackathon(String nome, String regolamento, LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio,
                                 LocalDateTime dataFine, String luogo, double premioInDenaro, int dimensioneMassimoTeam,
                                 StatoHackathon stato, Organizzatore organizzatore, Giudice giudice, List<Mentore> mentori){
        if(!verificaRequisiti(scadenzaIscrizione,dataInizio,dataFine)) return false;
        builder.reset();
        builder.setInfo(nome,regolamento,scadenzaIscrizione,dataInizio,dataFine,luogo,premioInDenaro,dimensioneMassimoTeam,stato,organizzatore);
        builder.setGiudice(giudice);
        builder.setMentori(mentori);
        Hackathon hackathon = builder.getResult();
        persistence.create(hackathon);
        return hackathon != null;
    }

    /**
     * Verifica che le date siano valide.
     */
    public boolean verificaRequisiti( LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio, LocalDateTime dataFine){
        if(scadenzaIscrizione.isBefore(LocalDateTime.now()) || dataInizio.isBefore(LocalDateTime.now())
                || dataFine.isBefore(LocalDateTime.now())) return false;
        if (scadenzaIscrizione.isAfter(dataInizio)) return false;
        if (dataInizio.isAfter(dataFine)) return false;
        return true;
    }


    public Hackathon getHackathonByID(long idHackathon) {
        return persistence.findById(idHackathon);
    }

    public StatoHackathon getStatoHackathon(Hackathon hackathon) {
        return hackathon.getStato();
    }

    /**
     * Verifica se almeno un hackathon nella lista si trova in uno degli stati specificati.
     * <p>
     * Questo metodo controlla se esiste almeno un hackathon non null nella lista fornita
     * il cui stato corrente corrisponde a uno degli stati passati come parametri.
     * È utilizzato per validare le operazioni consentite solo in determinati stati
     * del ciclo di vita dell'hackathon (es. iscrizioni possibili solo se "IN_ISCRIZIONE").
     * </p>
     *
     * @param listaHackathon Lista di hackathon da verificare. Può contenere valori null
     *                       che verranno ignorati durante la verifica.
     * @param stati Array di stati ammessi da verificare (es. {@link StatoHackathon#IN_ISCRIZIONE},
     *              {@link StatoHackathon#CONCLUSO}). Almeno uno di questi stati deve essere
     *              presente per ottenere un risultato positivo.
     * @return {@code true} se almeno un hackathon nella lista è in uno degli stati specificati,
     *         {@code false} se:
     *         <ul>
     *           <li>La lista è null o vuota</li>
     *           <li>L'array di stati è null o vuoto</li>
     *           <li>Nessun hackathon nella lista corrisponde agli stati specificati</li>
     *         </ul>
     */
    public boolean checkStato(List<Hackathon> listaHackathon, StatoHackathon... stati) {
        if (listaHackathon == null || listaHackathon.isEmpty() || stati == null || stati.length == 0) {
            return false;
        }

        for (Hackathon h : listaHackathon) {
            if (h == null) continue;

            StatoHackathon statoAttuale = h.getStato();
            for (StatoHackathon stato : stati) {
                if (statoAttuale == stato) {
                    return true;
                }
            }
        }
        return false;
    }
}
package it.unicam.cs.service;

import it.unicam.cs.dto.ClassificaTeamDTO;
import it.unicam.cs.model.*;
import it.unicam.cs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class HackathonService {
    private final HackathonRepository repository;
    private final AccountRepository accountRepository;



    public HackathonService(HackathonRepository repository, AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;

    }
    public boolean creaHackathon(String nome, String regolamento, LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio,
                                 LocalDateTime dataFine, String luogo, double premioInDenaro, int dimensioneMassimoTeam,
                                 StatoHackathon stato, Long idorganizzatore, Long idgiudice, List<Long> idmentori){
        if(!verificaRequisiti(scadenzaIscrizione,dataInizio,dataFine)) return false;
        Builder builder = new HackathonBuilder();
        builder.reset();
        Account organizzatore = accountRepository.findById(idorganizzatore)
                .orElse(null);
        if(organizzatore == null) return false;
        if(organizzatore.getRuolo()!= Ruolo.STAFF)return false;
        Account giudice = accountRepository.findById(idgiudice)
                .orElse(null);
        if(giudice == null) return false;
        if(giudice.getRuolo()!= Ruolo.STAFF)return false;
        List<Account> mentori = accountRepository.findAllById(idmentori);
        if(mentori.isEmpty()) return false;
        if(mentori.stream().anyMatch(m -> m.getRuolo()!=Ruolo.STAFF)) return false;
        builder.setInfo(nome,regolamento,scadenzaIscrizione,dataInizio,dataFine,luogo,premioInDenaro,
                dimensioneMassimoTeam,stato,organizzatore);
        builder.setGiudice(giudice);
        builder.setMentori(mentori);
        Hackathon hackathon = builder.getResult();
        repository.save(hackathon);
        return true;
    }

    /**
     * Verifica che le date siano valide.
     */
    public boolean verificaRequisiti( LocalDateTime scadenzaIscrizione, LocalDateTime dataInizio, LocalDateTime dataFine){
        if(scadenzaIscrizione.isBefore(LocalDateTime.now()) || dataInizio.isBefore(LocalDateTime.now())
                || dataFine.isBefore(LocalDateTime.now())) return false;
        if (!scadenzaIscrizione.isBefore(dataInizio)) return false;
        return !dataInizio.isAfter(dataFine);
    }


    public Hackathon getHackathonByID(long idHackathon) {
        return repository.findById(idHackathon).orElse(null);
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

        Set<StatoHackathon> statiSet = Set.of(stati);
        return listaHackathon.stream()
                .filter(Objects::nonNull)
                .anyMatch(h -> statiSet.contains(h.getStato()));
    }

    public boolean modificaHackathon(Hackathon hackathon,String nome,String regolamento,LocalDateTime scadenzaIscrizione,LocalDateTime dataInizio,LocalDateTime dataFine,String luogo,double premioInDenaro){
        if(hackathon == null) return false;
        if(!verificaRequisiti(scadenzaIscrizione,dataInizio,dataFine)) return false;
        hackathon.setNome(nome);
        hackathon.setRegolamento(regolamento);
        hackathon.setScadenzaIscrizione(scadenzaIscrizione);
        hackathon.setDataInizio(dataInizio);
        hackathon.setDataFine(dataFine);
        hackathon.setLuogo(luogo);
        hackathon.setPremioInDenaro(premioInDenaro);
        repository.save(hackathon);
        return true;

    }


}
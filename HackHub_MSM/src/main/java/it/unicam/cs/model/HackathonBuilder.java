package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Implementazione concreta del pattern Builder per la costruzione di un {@link Hackathon}.
 * Consente di assemblare un hackathon in più passi garantendo che le informazioni
 * principali siano impostate prima di giudice e mentori.
 */
public class HackathonBuilder implements Builder{
    Hackathon hackathon;

    /** Costruisce un'istanza di {@code HackathonBuilder}. */
    public HackathonBuilder(){}
    /**
     * Reimposta il builder impostando l'hackathon corrente a {@code null}.
     * Da chiamare prima di iniziare la costruzione di un nuovo hackathon.
     */
    @Override
    public void reset() {
        hackathon = null;
    }
    /**
     * Imposta il giudice dell'hackathon in costruzione.
     *
     * @param giudice l'account da assegnare come giudice
     * @throws IllegalStateException se {@link #setInfo} non è stato ancora chiamato
     */
    @Override
    public void setGiudice(Account giudice) {
        if(hackathon == null) throw new IllegalStateException("Hackathon non creato");
        hackathon.setGiudice(giudice);
    }
    /**
     * Imposta la lista dei mentori dell'hackathon in costruzione.
     *
     * @param mentori la lista degli account da assegnare come mentori
     * @throws IllegalStateException se {@link #setInfo} non è stato ancora chiamato
     */
    @Override
    public void setMentori(List<Account> mentori) {
        if(hackathon == null) throw new IllegalStateException("Hackathon non creato");
        hackathon.setMentori(mentori);
    }
    /**
     * Inizializza un nuovo hackathon con le informazioni principali.
     * Deve essere chiamato come primo passo della costruzione.
     *
     * @param nome                nome dell'hackathon
     * @param regolamento         testo del regolamento
     * @param scadenzaIscrizioni  data e ora di scadenza delle iscrizioni
     * @param dataInizio          data e ora di inizio
     * @param dataFine            data e ora di fine
     * @param luogo               luogo dell'evento
     * @param premio              premio in denaro per i vincitori
     * @param dimensione          dimensione massima consentita per ogni team
     * @param stato               stato iniziale dell'hackathon
     * @param organizzatore       account dell'organizzatore
     */
    @Override
    public void setInfo(String nome, String regolamento,LocalDateTime scadenzaIscrizioni,LocalDateTime dataInizio, LocalDateTime dataFine,
                        String luogo, double premio, int dimensione, StatoHackathon stato,Account organizzatore) {
        hackathon= new Hackathon();
        hackathon.setNome(nome);
        hackathon.setRegolamento(regolamento);
        hackathon.setScadenzaIscrizione(scadenzaIscrizioni);
        hackathon.setDataInizio(dataInizio);
        hackathon.setDataFine(dataFine);
        hackathon.setLuogo(luogo);
        hackathon.setPremioInDenaro(premio);
        hackathon.setDimensioneMassimoTeam(dimensione);
        hackathon.setStato(stato);
        hackathon.setOrganizzatore(organizzatore);
    }
    /**
     * Restituisce l'hackathon costruito.
     *
     * @return l'istanza di {@link Hackathon} completamente configurata
     * @throws IllegalStateException se {@link #setInfo} non è stato ancora chiamato
     */
    @Override
    public Hackathon getResult() {
        if(hackathon == null) throw new IllegalStateException("Hackathon non creato");
        return hackathon;
    }
}

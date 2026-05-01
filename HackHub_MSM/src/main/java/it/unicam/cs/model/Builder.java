package it.unicam.cs.model;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Interfaccia che definisce il contratto del pattern Builder per la creazione di un {@link Hackathon}.
 * Permette di costruire un hackathon passo dopo passo, separando la costruzione
 * dalla rappresentazione finale dell'oggetto.
 */
public interface Builder {
    /**
     * Reimposta il builder eliminando l'hackathon in costruzione.
     * Da chiamare prima di iniziare la costruzione di un nuovo hackathon.
     */
    void reset();
    /**
     * Imposta il giudice dell'hackathon in costruzione.
     *
     * @param giudice l'account da assegnare come giudice
     * @throws IllegalStateException se {@link #setInfo} non è stato ancora chiamato
     */
    void setGiudice(Account giudice);
    /**
     * Imposta la lista dei mentori dell'hackathon in costruzione.
     *
     * @param mentori la lista degli account da assegnare come mentori
     * @throws IllegalStateException se {@link #setInfo} non è stato ancora chiamato
     */
    void setMentori(List<Account> mentori);

    /**
     * Inizializza l'hackathon con le informazioni principali.
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
    void setInfo(String nome, String regolamento,LocalDateTime scadenzaIscrizioni, LocalDateTime dataInizio, LocalDateTime dataFine,
                 String luogo, double premio, int dimensione, StatoHackathon stato, Account organizzatore);
    /**
     * Restituisce l'hackathon costruito.
     *
     * @return l'istanza di {@link Hackathon} completamente configurata
     * @throws IllegalStateException se {@link #setInfo} non è stato ancora chiamato
     */
    Hackathon getResult();
    }

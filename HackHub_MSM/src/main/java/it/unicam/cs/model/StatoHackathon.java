package it.unicam.cs.model;
/**
 * Enumerazione che rappresenta i possibili stati del ciclo di vita di un hackathon.
 */
public enum StatoHackathon {
    IN_ISCRIZIONE,
    IN_CORSO,
    IN_VALUTAZIONE,
    CONCLUSO;
    /**
     * Restituisce il valore dell'enumerazione corrispondente alla stringa fornita,
     * ignorando le differenze tra maiuscole e minuscole.
     *
     * @param stringa la stringa da convertire (es. {@code "in_corso"} o {@code "IN_CORSO"})
     * @return il valore di {@code StatoHackathon} corrispondente
     * @throws IllegalArgumentException se la stringa non corrisponde a nessun valore valido
     */
    public static StatoHackathon fromString(String stringa) {
        for (StatoHackathon stato : values()) {
            if (stato.name().equalsIgnoreCase(stringa)) {
                return stato;
            }
        }
        throw new IllegalArgumentException("Stato non valido: " + stringa);
    }
}

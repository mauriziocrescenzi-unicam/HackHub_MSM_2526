package it.unicam.cs.model;
/**
 * Enumerazione che rappresenta i possibili stati di una segnalazione.
 */
public enum StatoSegnalazione {
    DA_GESTIRE,
    GESTITA,
    RIFIUTATA;
    /**
     * Restituisce il valore dell'enumerazione corrispondente alla stringa fornita,
     * ignorando le differenze tra maiuscole e minuscole.
     *
     * @param stringa la stringa da convertire (es. {@code "gestita"} o {@code "GESTITA"})
     * @return il valore di {@code StatoSegnalazione} corrispondente, oppure {@code null}
     *         se la stringa non corrisponde a nessun valore valido
     */
    public static StatoSegnalazione fromString(String stringa) {
        for (StatoSegnalazione stato : values()) {
            if (stato.name().equalsIgnoreCase(stringa)) {
                return stato;
            }
        }
        return null;
    }
}

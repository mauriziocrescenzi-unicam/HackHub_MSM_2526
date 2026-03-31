package it.unicam.cs.model;

public enum StatoSegnalazione {
    DA_GESTIRE,
    GESTITA,
    RIFIUTATA;
    public static StatoSegnalazione fromString(String stringa) {
        for (StatoSegnalazione stato : values()) {
            if (stato.name().equalsIgnoreCase(stringa)) {
                return stato;
            }
        }
        return null;
    }
}

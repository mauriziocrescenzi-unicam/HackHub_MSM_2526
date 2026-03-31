package it.unicam.cs.model;

public enum StatoHackathon {
    IN_ISCRIZIONE,
    IN_CORSO,
    IN_VALUTAZIONE,
    CONCLUSO;

    public static StatoHackathon fromString(String stringa) {
        for (StatoHackathon stato : values()) {
            if (stato.name().equalsIgnoreCase(stringa)) {
                return stato;
            }
        }
        throw new IllegalArgumentException("Stato non valido: " + stringa);
    }
}

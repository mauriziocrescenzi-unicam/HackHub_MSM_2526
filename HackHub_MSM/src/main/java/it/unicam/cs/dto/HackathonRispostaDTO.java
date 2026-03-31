package it.unicam.cs.dto;

import it.unicam.cs.model.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record HackathonRispostaDTO(
        @NotNull String nome,
        @NotNull String regolamento,
        @NotNull LocalDateTime scadenzaIscrizione,
        @NotNull LocalDateTime dataInizio,
        @NotNull LocalDateTime dataFine,
        @NotNull String luogo,
        @NotNull Double premioInDenaro,
        @NotNull Integer dimensioneMassimoTeam,
        @NotNull StatoHackathon stato,
        @NotNull Organizzatore organizzatore,
        @NotNull Giudice giudice,
        @NotNull List<Mentore> mentori
) {
    public static HackathonRispostaDTO fromHackathon(Hackathon hackathon) {
        return new HackathonRispostaDTO(hackathon.getNome(),hackathon.getRegolamento(),hackathon.getScadenzaIscrizione(),
                hackathon.getDataInizio(),hackathon.getDataFine(),hackathon.getLuogo(),hackathon.getPremioInDenaro(),
                hackathon.getDimensioneMassimoTeam(),hackathon.getStato(),hackathon.getOrganizzatore(),hackathon.getGiudice(),
                hackathon.getMentori());
    }
}

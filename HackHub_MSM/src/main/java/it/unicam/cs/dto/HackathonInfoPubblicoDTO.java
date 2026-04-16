package it.unicam.cs.dto;

import it.unicam.cs.model.Hackathon;

public record HackathonInfoPubblicoDTO(String nome, String regolamento) {
    public static HackathonInfoPubblicoDTO fromHackathon(Hackathon h) {
        return new HackathonInfoPubblicoDTO(h.getNome(), h.getRegolamento());
    }
}
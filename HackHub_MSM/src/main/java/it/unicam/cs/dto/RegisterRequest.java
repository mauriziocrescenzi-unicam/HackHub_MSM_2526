package it.unicam.cs.dto;

public record RegisterRequest(
        String email,
        String password,
        String nome,
        String cognome,
        String ruolo
) {}

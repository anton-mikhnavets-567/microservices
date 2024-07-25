package com.example.ticketservice.models.dtos;

public record HistoryAttDTO(
        Integer ticketID,
        String action,
        String description
) {
}

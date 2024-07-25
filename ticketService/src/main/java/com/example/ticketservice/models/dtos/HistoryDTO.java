package com.example.ticketservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HistoryDTO (
        @JsonFormat(pattern="dd MMMM yyyy, HH:mm", locale="en_US")
        LocalDateTime timestamp,
        String action,
        String username,
        String description
){
}

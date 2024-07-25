package com.example.attachmentservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HistoryDTO(
        Integer ticketID,
        String action,
        String description
){
}

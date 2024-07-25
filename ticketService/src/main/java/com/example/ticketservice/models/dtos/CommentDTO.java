package com.example.ticketservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CommentDTO (
        String username,
        String text,
        @JsonFormat(pattern="dd MMMM yyyy, HH:mm", locale="en_US")
        LocalDateTime timestamp
){
}

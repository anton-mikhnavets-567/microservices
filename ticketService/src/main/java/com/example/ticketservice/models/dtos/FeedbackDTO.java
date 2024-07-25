package com.example.ticketservice.models.dtos;

import java.time.LocalDateTime;

public record FeedbackDTO (
        Integer rate,
        LocalDateTime timestamp
){
}

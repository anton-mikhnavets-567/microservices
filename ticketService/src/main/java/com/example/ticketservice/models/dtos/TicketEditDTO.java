package com.example.ticketservice.models.dtos;

import com.example.ticketservice.models.enums.TicketUrgency;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketEditDTO {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private TicketUrgency urgency;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate desiredResolutionDate;
}








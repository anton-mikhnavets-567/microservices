package com.example.ticketservice.models.dtos;

import com.example.ticketservice.models.enums.TicketUrgency;
import com.example.ticketservice.models.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketCreateDTO {
    private Integer categoryID;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private TicketUrgency urgency;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate desiredResolutionDate;
    private String comment;
}

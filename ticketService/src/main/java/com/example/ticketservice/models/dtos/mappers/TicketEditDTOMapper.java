package com.example.ticketservice.models.dtos.mappers;

import com.example.ticketservice.models.dtos.TicketEditDTO;
import com.example.ticketservice.models.entities.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketEditDTOMapper {

    public TicketEditDTO toDTO(Ticket t) {
        var dto = new TicketEditDTO();

        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setCategoryId(t.getCategory().getId());
        dto.setDescription(t.getDescription());
        dto.setUrgency(t.getUrgency());
        dto.setDesiredResolutionDate(t.getDesiredResolutionDate());

        return dto;
    }

    public Ticket toEntity(TicketEditDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setId(dto.getId());
        ticket.setName(dto.getName());
        ticket.setDescription(dto.getDescription());
        ticket.setUrgency(dto.getUrgency());
        ticket.setDesiredResolutionDate(dto.getDesiredResolutionDate());
        return ticket;
    }
}

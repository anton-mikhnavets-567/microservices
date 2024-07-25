package com.example.ticketservice.models.dtos.mappers;

import com.example.ticketservice.models.dtos.TicketCreateDTO;
import com.example.ticketservice.models.dtos.TicketDTO;
import com.example.ticketservice.models.dtos.UserInfoDTO;
import com.example.ticketservice.models.entities.Category;
import com.example.ticketservice.models.entities.Ticket;
import com.example.ticketservice.models.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class TicketCreateDTOMapper{
    public Ticket toEntity(TicketCreateDTO dto, Category category, Integer userID) {
        Ticket ticket = new Ticket();

        ticket.setName(dto.getName());
        ticket.setCategory(category);
        ticket.setDescription(dto.getDescription());
        ticket.setUrgency(dto.getUrgency());
        ticket.setDesiredResolutionDate(dto.getDesiredResolutionDate());
        ticket.setOwnerID(userID);

        return ticket;
    }
}

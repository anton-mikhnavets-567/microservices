package com.example.ticketservice.models.dtos.mappers;


import com.example.ticketservice.models.dtos.TicketDTO;
import com.example.ticketservice.models.dtos.UserInfoDTO;
import com.example.ticketservice.models.entities.Ticket;
import com.example.ticketservice.models.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TicketDTOMapper{
    public TicketDTO toDTO(Ticket t, UserInfoDTO ow, UserInfoDTO ap, UserInfoDTO as) {
        var dto = new TicketDTO();

        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setCategory(t.getCategory().getName());
        dto.setCreatedOn(t.getCreatedOn());
        dto.setDesiredResolutionDate(t.getDesiredResolutionDate());
        dto.setOwnerEmail(ow.email());
        dto.setOwnerRole(UserRole.valueOf(ow.role()));
        dto.setState(t.getState());
        dto.setUrgency(t.getUrgency());
        if (ap != null) {
            dto.setApproverEmail(ap.email());
        }
        if (as != null) {
            dto.setAssigneeEmail(as.email());
        }
        return dto;
    }

    public List<TicketDTO> allToDTOs(Map<Integer, UserInfoDTO> users, List<Ticket> tickets) {
        List<TicketDTO> ticketDTOs = new ArrayList<>();
        for (Ticket t : tickets){
            TicketDTO dto = toDTO(t, users.getOrDefault(t.getOwnerID(), null), users.getOrDefault(t.getApproverID(), null), users.getOrDefault(t.getAssigneeID(), null));
//            UserInfoDTO owner = users.get(t.getOwnerID());
//            Integer approverId = t.getApproverID();
//            UserInfoDTO approver = approverId != null ? users.get(approverId) : null;
//            Integer assigneeId = t.getAssigneeID();
//            UserInfoDTO assignee = assigneeId != null ? users.get(assigneeId) : null;
//
//            TicketDTO dto = toDTO(t, owner, approver, assignee);

            ticketDTOs.add(dto);
        }
        return ticketDTOs;
    }
}

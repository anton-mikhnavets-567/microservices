package com.example.ticketservice.models.dtos.mappers;

import com.example.ticketservice.TicketServiceClient;
import com.example.ticketservice.models.dtos.TicketStatDTO;
import com.example.ticketservice.models.dtos.UserInfoDTO;
import com.example.ticketservice.models.entities.History;
import com.example.ticketservice.models.entities.Ticket;
import com.example.ticketservice.models.enums.TicketState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class TicketStatDTOMapper {
    private final TicketServiceClient ticketSC;

    public TicketStatDTO toDTO(Ticket ticket, String assigneeUsername) {
        TicketStatDTO dto = new TicketStatDTO();

        dto.setCategory(ticket.getCategory().getName());
        dto.setUrgency(ticket.getUrgency());
        dto.setCreatedOn(ticket.getCreatedOn());
        LocalDateTime from = ticket.getCreatedOn().atTime(12,1);

        LocalDateTime to = ticket.getHistoryRecords().stream()
                .filter(h -> h.getDescription().matches(".*на '(Выполнена|Отменена|Отклонена)'"))
                .findFirst()
                .map(History::getTimestamp)
                .orElse(LocalDateTime.now());

        //long secondsBetween = ChronoUnit.SECONDS.between(from, to);
        long daysBetween = Period.between(from.toLocalDate(), to.toLocalDate()).getDays();
        //double daysBetween = secondsBetween / (60.0 * 60.0 * 24.0);
        Double ttr =  Math.round(daysBetween * 10.0) / 10.0;

        dto.setTimeToRes(ttr);
        dto.setAssignee(assigneeUsername);

        dto.setInProgress(ticket.getState() == TicketState.In_progress);
        return dto;
    }

    public List<TicketStatDTO> allToDTO(List<Ticket> tickets, List<UserInfoDTO> assignees){
        List<TicketStatDTO> stats = new ArrayList<>();
        for (Ticket ticket : tickets){
            String username = null;
            if(ticket.getAssigneeID() != null){
                username = assignees.stream()
                        .filter(u -> Objects.equals(u.id(), ticket.getAssigneeID()))
                        .findFirst()
                        .get().username();
            }
            stats.add(toDTO(ticket, username));
        }
        return stats;
    }
}

package com.example.ticketservice.models.dtos.mappers;

import com.example.ticketservice.models.dtos.HistoryAttDTO;
import com.example.ticketservice.models.entities.History;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HistoryAttDTOMapper {

    public History toEntity(HistoryAttDTO dto, Integer userID){
        History h = new History();

        h.setTimestamp(LocalDateTime.now());
        h.setAction(dto.action());
        h.setDescription(dto.description());
        h.setUserID(userID);
        h.setTicketId(dto.ticketID());

        return h;
    }

    public List<History> allToEntity(List<HistoryAttDTO> dtos, Integer userID){
        List<History> h = new ArrayList<>();
        for (HistoryAttDTO dto : dtos){
            h.add(toEntity(dto, userID));
        }
        return h;
    }
}

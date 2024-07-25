package com.example.ticketservice.models.dtos.mappers;

import com.example.ticketservice.TicketServiceClient;
import com.example.ticketservice.models.dtos.*;
import com.example.ticketservice.models.entities.Comment;
import com.example.ticketservice.models.entities.History;
import com.example.ticketservice.models.entities.Ticket;
import com.example.ticketservice.models.dtos.TicketDTO;
import com.example.ticketservice.models.dtos.TicketFullDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TicketFullDTOMapper extends TicketDTOMapper {
    private final TicketServiceClient ticketSC;
    public TicketFullDTO toDTO(Ticket t, UserInfoDTO ow, UserInfoDTO ap, UserInfoDTO as, List<AttachmentDTO> att) {
        TicketDTO shortDTO = super.toDTO(t,ow,ap,as);
        TicketFullDTO fullDTO = new TicketFullDTO();
        BeanUtils.copyProperties(shortDTO,fullDTO);
        fullDTO.setDescription(t.getDescription());
        fullDTO.setOwnerUsername(ow.username());

        Set<Integer> ids = Stream.concat(t.getComments().stream().map(Comment::getUserID),
                                            t.getHistoryRecords().stream().map(History::getUserID))
                .collect(Collectors.toSet());

        Map<Integer, String> users = ticketSC.getUsernamesByIDs(ids);

        if(ap != null) {
            fullDTO.setApproverUsername(ap.username());
        }
        if(as != null) {
            fullDTO.setAssigneeUsername(as.username());
        }

        fullDTO.setHistoryRecords(t.getHistoryRecords().stream()
                .map(hr -> new HistoryDTO(hr.getTimestamp(),
                                        hr.getAction(), users.get(hr.getUserID()),
                                        hr.getDescription()))
                .toList());

        if (t.getFeedbacks() != null) {
            fullDTO.setFeedbacks(t.getFeedbacks().stream()
                    .map(f -> new FeedbackDTO(f.getRate(), f.getTimestamp()))
                    .toList());
        }
        if(att.size() != 0){
            fullDTO.setAttachments(att);
        }
        if(t.getComments() != null) {
            fullDTO.setComments(t.getComments().stream()
                    .map(c -> new CommentDTO(users.get(c.getUserID()), c.getText(),
                            c.getTimestamp()))
                    .toList());
        }

        return fullDTO;
    }
}

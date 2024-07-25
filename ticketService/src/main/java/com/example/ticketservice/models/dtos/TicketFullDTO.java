package com.example.ticketservice.models.dtos;

import com.example.ticketservice.models.entities.Attachment;
import com.example.ticketservice.models.entities.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketFullDTO extends TicketDTO {

    private String description;
    private String ownerUsername;
    private String assigneeUsername;
    private String approverUsername;
    private List<FeedbackDTO> feedbacks;
    private List<AttachmentDTO> attachments;
    private List<CommentDTO> comments;
    private List<HistoryDTO> historyRecords;
}

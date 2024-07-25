package com.example.attachmentservice;

import com.example.attachmentservice.models.dtos.AttachmentDTO;
import com.example.attachmentservice.models.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    @Query("select a from Attachment a where a.name = :name and a.ticketId = :ticketId")
    Attachment getAttachmentByNameAndTicketId(@Param("name") String name,
                                              @Param("ticketId") Integer ticketId);
    @Query(nativeQuery = true, value = "select * from Attachment a where a.ticket_id = :ticketID")
    List<Attachment> getAttachmentsByTicketId(@Param("ticketID") Integer ticketID);
    @Query(nativeQuery = true, value = "select * from Attachment")
    List<Attachment> getAll();
}

package com.example.attachmentservice;

import com.example.attachmentservice.models.dtos.AttachmentDTO;
import com.example.attachmentservice.models.dtos.HistoryDTO;
import com.example.attachmentservice.models.entities.Attachment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final AttachmentRepository attachmentRepository;
    private final AttachmentServiceClient attachmentSC;
    public Attachment getAttachmentByNameAndTicketId(String name, Integer ticketId) {
        return attachmentRepository.getAttachmentByNameAndTicketId(name, ticketId);
    }

    public void processAttachmentsForEdition(Integer ticketID,
                                             MultipartFile[] newFiles,
                                             String attToDeleteJson) {
        try{
            List<AttachmentDTO> attToDelete = objectMapper.readValue(attToDeleteJson, new TypeReference<>() {});
            if(newFiles!=null) {
                attachmentRepository.saveAll(addAttachmentsToList(newFiles, ticketID));
            }
            if(attToDelete!= null){
                deleteAttachments(attToDelete, ticketID);
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private List<Attachment> addAttachmentsToList(MultipartFile[] attachmentFiles, Integer ticketId){
        List<Attachment> attachmentList = new ArrayList<>();
        for (MultipartFile attachmentFile : attachmentFiles) {
            attachmentList.add(MultipartToAttachment(attachmentFile, ticketId));
        }
        return attachmentList;
    }

    private Attachment MultipartToAttachment(MultipartFile file, Integer ticketId){
        Attachment attachment = new Attachment();
        try {
            attachment.setContents(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        attachment.setTicketId(ticketId);
        attachment.setName(file.getOriginalFilename());
        return attachment;
    }


    private void deleteAttachments(List<AttachmentDTO> attToDel, Integer ticketID){
        Set<Integer> idsToDelete= new HashSet<>();
        List<HistoryDTO> records = new ArrayList<>();
        for (AttachmentDTO att : attToDel) {
            idsToDelete.add(att.id());
            records.add(new HistoryDTO(ticketID,
                    "Файл удалён",
                    "Файл удалён: " + att.fileName()));
        }
        attachmentRepository.deleteAllById(idsToDelete);
        attachmentSC.addHisRecForAtt(records);
    }

    public void addAttachmentsToNewTicket(MultipartFile[] ticketFiles, Integer ticketId){
        List<Attachment> attachments = addAttachmentsToList(ticketFiles, ticketId);
        List<HistoryDTO> records = new ArrayList<>();
        for (Attachment attachment : attachments){
            records.add(new HistoryDTO(ticketId,
                    "Файл прикреплён",
                    "Файл прикреплён: " + attachment.getName()));
        }
        attachmentRepository.saveAll(attachments);
        attachmentSC.addHisRecForAtt(records);
    }

    public List<AttachmentDTO> getAttachmentsByTicketId(Integer ticketId) {
        List<Attachment> att = attachmentRepository.getAttachmentsByTicketId(ticketId);
        return att.stream()
                .map(a -> new AttachmentDTO(a.getId(),a.getName()))
                .toList();
    }

    public List<AttachmentDTO> getAll() {
        List<Attachment> att = attachmentRepository.getAll();
        return att.stream()
                .map(a -> new AttachmentDTO(a.getId(),a.getName()))
                .toList();
    }
}

package com.example.attachmentservice;

import com.example.attachmentservice.models.dtos.AttachmentDTO;
import com.example.attachmentservice.models.entities.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @GetMapping("/download/{ticketId}/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName,
                                                 @PathVariable Integer ticketId) {

        Attachment attachment = attachmentService.getAttachmentByNameAndTicketId(fileName, ticketId);
        ByteArrayResource resource = new ByteArrayResource(attachment.getContents());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                .body(resource);
    }

    @GetMapping("/getAttachmentDTOs/{ticketId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentDTOs(@PathVariable Integer ticketId){
        return ResponseEntity.ok().body(attachmentService.getAttachmentsByTicketId(ticketId));
    }

    @PostMapping("/createAttachments/{ticketID}")
    public void createAttachments(@PathVariable Integer ticketID,
                                  @RequestParam(name = "ticketFiles",required = false) MultipartFile[] ticketFiles){
        attachmentService.addAttachmentsToNewTicket(ticketFiles, ticketID);
    }

    @PostMapping("/editAttachments/{ticketID}")
    public void editAttachments(@PathVariable Integer ticketID,
                                @RequestParam(name = "newFiles",required = false) MultipartFile[] newFiles,
                                @RequestParam(name = "filesToDelete", required = false) String filesToDeleteJson) throws IOException {
        attachmentService.processAttachmentsForEdition(ticketID, newFiles, filesToDeleteJson);
    }

    @GetMapping("/test")
    public ResponseEntity<List<AttachmentDTO>> test(){
        return ResponseEntity.ok().body(attachmentService.getAll());
    }
}

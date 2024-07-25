package com.example.mailsenderservice.models.dtos;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MailMessageDTO {

    @JsonProperty("to")
    private String[] to;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

}

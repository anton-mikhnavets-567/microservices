package com.example.mailsenderservice.kafka;

import com.example.mailsenderservice.MailSenderService;
import com.example.mailsenderservice.models.dtos.MailMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final MailSenderService mailSenderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "mail_topic", groupId = "mail_consumer")
    public void listen (String json){
        try {
            var dto = objectMapper.readValue(json, MailMessageDTO.class);
            mailSenderService.sendNewMail(dto);
        }
        catch (Exception e){
            System.out.println("EXCEPTION:" + e.getMessage());
        }
    }
}

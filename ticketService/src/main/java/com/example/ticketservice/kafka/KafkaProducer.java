package com.example.ticketservice.kafka;

import com.example.ticketservice.models.dtos.MailMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(String to, String subject, String body){
        sendMessage(new String[]{to}, subject, body);
    }
    public void sendMessage(String[] to, String subject, String body){
        try {
            String message = objectMapper.writeValueAsString(new MailMessageDTO(to, subject, body));
            kafkaTemplate.send("mail_topic", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}

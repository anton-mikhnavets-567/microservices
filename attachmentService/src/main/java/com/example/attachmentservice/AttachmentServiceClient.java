package com.example.attachmentservice;

import com.example.attachmentservice.models.dtos.HistoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentServiceClient {
    private final WebClient webClient;

    public void addHisRecForAtt(List<HistoryDTO> records) {
        webClient
                .post()
                .uri("http://localhost:8585/tickets" + "/addHisRecForAtt")
                .bodyValue(records);
//                .retrieve()
//                .toBodilessEntity()
//                .block();
    }
}

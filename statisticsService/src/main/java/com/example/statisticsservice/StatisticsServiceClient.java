package com.example.statisticsservice;

import com.example.statisticsservice.models.dtos.TicketStatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceClient {
    private final WebClient webClient;

    public List<TicketStatDTO> getStatDTOsFromTickets() {
        return webClient
                .get()
                .uri("http://localhost:8585/tickets/getStatDTOs")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TicketStatDTO>>() {})
                .block();
    }
}

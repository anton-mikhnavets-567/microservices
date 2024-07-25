package com.example.ticketservice;

import com.example.ticketservice.models.dtos.AttachmentDTO;
import com.example.ticketservice.models.dtos.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketServiceClient {
    private final WebClient webClient;

    public List<String> figureCategoryWhereBest(String email){
        return webClient
                .post()
                .uri("http://localhost:8585/stats"+"/figureCategoryWhereBest")
                .bodyValue(email)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block();
    }

    public Integer getUserID(String email) {
        try {
            return webClient
                    .get()
                    .uri("http://localhost:8585/users/getUserIDByEmail/" + email)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve user ID", e);
        }
    }

    public List<Integer> getAllEmployeeIDs() {
        return webClient
                .get()
                .uri("http://localhost:8585/users"+"/getAllEmployeeIDs")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Integer>>() {})
                .block();
    }

    public List<Integer> getAllEmployeeAndManagerIDs() {
        return webClient
                .get()
                .uri("http://localhost:8585/users"+"/getAllEmployeeAndManagerIDs")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Integer>>() {})
                .block();
    }

    public Map<Integer, UserInfoDTO> getUserInfoDTOs(Set<Integer> uniqueIDs) {
        return webClient
                .post()
                .uri("http://localhost:8585/users"+"/getUserInfoDTOs")
                .bodyValue(uniqueIDs)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, UserInfoDTO>>() {})
                .block();
    }
    public Map<Integer, String> getUsernamesByIDs(Set<Integer> ids) {
        return webClient
                .post()
                .uri("http://localhost:8585/users"+"/getUsernamesByIDs")
                .bodyValue(ids)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer, String>>() {})
                .block();
    }

    public List<AttachmentDTO> getAttachmentDTOs(Integer ticketId) {
        return webClient
                .get()
                .uri("http://localhost:8585/attachments"+"/getAttachmentDTOs/" + ticketId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AttachmentDTO>>() {})
                .block();
    }

    public String[] getEmailsByRole(String role) {
        return webClient
                .get()
                .uri("http://localhost:8585/users"+"/getEmailsByRole/" + role)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<String[]>() {})
                .block();
    }

    public String getEmailByID(Integer userID) {
        return webClient
                .get()
                .uri("http://localhost:8585/users/getEmailByID/" + userID)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<UserInfoDTO> getAllEngineers() {
        return webClient
                .get()
                .uri("http://localhost:8585/users/getAllEngineers")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserInfoDTO>>() {})
                .block();
    }
}

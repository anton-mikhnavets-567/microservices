package com.example.statisticsservice.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class StatDTO {
    private List<TicketStatDTO> ticketStatList;
    private List<ProblemsDTO> problemsList;
    private List<EngineerTierForCategoryDTO> engTierListByCat;
}

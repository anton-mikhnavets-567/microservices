package com.example.statisticsservice.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class EngineerTierForCategoryDTO {
    String category;
    List<String> engTierList;
}

package com.example.statisticsservice;

import com.example.statisticsservice.models.dtos.StatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/getBestCategories")
    public ResponseEntity<List<String>> getBestCategories() {
        return ResponseEntity.ok().body(statisticsService.figureCategoryWhereBest());
    }

    @GetMapping("/getStatistics")
    public ResponseEntity<StatDTO> getStatistics() {
        return ResponseEntity.ok().body(statisticsService.computeTotalStats());
    }
}

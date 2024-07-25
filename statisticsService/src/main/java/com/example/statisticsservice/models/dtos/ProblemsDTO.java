package com.example.statisticsservice.models.dtos;

import com.example.statisticsservice.models.enums.PeriodEnum;

public record ProblemsDTO(
        String firstMetricLeader,
        String secondMetricLeader,
        String thirdMetricLeader,
        String allMetricLeader,
        PeriodEnum period
) {

}

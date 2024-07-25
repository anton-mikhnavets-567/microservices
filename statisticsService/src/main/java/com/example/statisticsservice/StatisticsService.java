package com.example.statisticsservice;

import com.example.statisticsservice.models.dtos.*;
import com.example.statisticsservice.models.enums.PeriodEnum;
import com.example.statisticsservice.models.enums.TicketUrgency;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsServiceClient statisticsSC;

    private String getUsernameFromContext(){
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return token.getTokenAttributes().get("preferred_username").toString();
    }


    public StatDTO computeTotalStats(){
        List<TicketStatDTO> ticketStatDTOs = statisticsSC.getStatDTOsFromTickets();
        List<ProblemsDTO> problemsDTOs = new ArrayList<>();

        List<TicketStatDTO> lwTicketStats = getTicketStatsForPeriod(LocalDateTime.now().minus(1, ChronoUnit.WEEKS), ticketStatDTOs);
        List<TicketStatDTO> lmTicketStats = getTicketStatsForPeriod(LocalDateTime.now().minus(1, ChronoUnit.MONTHS), ticketStatDTOs);
        List<TicketStatDTO> l3mTicketStats = getTicketStatsForPeriod(LocalDateTime.now().minus(3, ChronoUnit.MONTHS), ticketStatDTOs);
        List<TicketStatDTO> lyTicketStats = getTicketStatsForPeriod(LocalDateTime.now().minus(1, ChronoUnit.YEARS), ticketStatDTOs);

        problemsDTOs.add(initializeProblemsDTO(lwTicketStats, PeriodEnum.Week));
        problemsDTOs.add(initializeProblemsDTO(lmTicketStats, PeriodEnum.Month));
        problemsDTOs.add(initializeProblemsDTO(l3mTicketStats, PeriodEnum.ThreeMonths));
        problemsDTOs.add(initializeProblemsDTO(lyTicketStats, PeriodEnum.Year));

        List<EngineerTierForCategoryDTO> engTierListByCat = calculateEngTierLists(l3mTicketStats);// CHANGE TO LM
        return new StatDTO(ticketStatDTOs,problemsDTOs, engTierListByCat);
    }



    public List<String> figureCategoryWhereBest(){
        List<String> res = new ArrayList<>();
        List<TicketStatDTO> ticketStatDTOs = statisticsSC.getStatDTOsFromTickets();
        List<TicketStatDTO> lmTicketStats = getTicketStatsForPeriod(LocalDateTime.now().minus(3, ChronoUnit.MONTHS), ticketStatDTOs);
        List<EngineerTierForCategoryDTO> engTierListByCat = calculateEngTierLists(lmTicketStats);
        String assigneeName = getUsernameFromContext();
        for (EngineerTierForCategoryDTO dto : engTierListByCat) {
            if (dto.getEngTierList().size() > 0 && dto.getEngTierList().get(0).equals(assigneeName)) {
                res.add(dto.getCategory());
            }
        }
        return res;
    }

    private List<TicketStatDTO> getTicketStatsForPeriod(LocalDateTime period, List<TicketStatDTO> ticketStats){
        return  ticketStats.stream()
                .filter(t -> t.getCreatedOn().isAfter(ChronoLocalDate.from(period)))
                .collect(Collectors.toList());
    }

    private List<EngineerTierForCategoryDTO> calculateEngTierLists(List<TicketStatDTO> stats){

        Map<String, List<TicketStatDTO>> statsByCategory = stats.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(TicketStatDTO::getCategory));
        List<EngineerTierForCategoryDTO> tierLists = new ArrayList<>();

        // Подсчёт активных тикетов для каждого assignee
        Map<String, Integer> activeTicketsByAssignee = stats.stream()
                .filter(t -> t.getAssignee() != null && t.isInProgress())
                .collect(Collectors.groupingBy(
                        TicketStatDTO::getAssignee,
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));


        for (Map.Entry<String, List<TicketStatDTO>> entry : statsByCategory.entrySet()) {
            String category = entry.getKey();
            List<TicketStatDTO> tickets = entry.getValue();

            // Группировка тикетов по assignee
            Map<String, List<TicketStatDTO>> ticketsByAssignee = tickets.stream()
                    .collect(Collectors.groupingBy(TicketStatDTO::getAssignee));

            // Вычисление score для каждого assignee
            Map<String, Double> assigneeScores = new HashMap<>();
            for (Map.Entry<String, List<TicketStatDTO>> assigneeEntry : ticketsByAssignee.entrySet()) {
                String assignee = assigneeEntry.getKey();
                List<TicketStatDTO> assigneeTickets = assigneeEntry.getValue();
                int activeTickets = activeTicketsByAssignee.get(assignee) != null ? activeTicketsByAssignee.get(assignee) : 0;
                double score = calculateScore(assigneeTickets, activeTickets);
                assigneeScores.put(assignee, score);
            }

            // Сортировка assignee по score
            List<String> sortedAssignees = assigneeScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            tierLists.add(new EngineerTierForCategoryDTO(category, sortedAssignees));
        }

        return tierLists;
    }

    private double calculateScore(List<TicketStatDTO> tickets, int activeTickets) {
        long totalCount = tickets.size();
        long criticalCount = tickets.stream().filter(t -> t.getUrgency() == TicketUrgency.Critical).count();
        long highCount = tickets.stream().filter(t -> t.getUrgency() == TicketUrgency.High).count();
        long averageCount = tickets.stream().filter(t -> t.getUrgency() == TicketUrgency.Average).count();
        long lowCount = tickets.stream().filter(t -> t.getUrgency() == TicketUrgency.Low).count();
        double avgTimeToRes = tickets.stream().mapToDouble(TicketStatDTO::getTimeToRes).average().orElse(0);

        return ((totalCount * 0.5)
                + (criticalCount * 1.0)
                + (highCount * 0.7)
                + (averageCount * 0.4)
                + (lowCount * 0.1)
                + (avgTimeToRes * 0.7))
                * ((double) (10 - activeTickets) /10);
    }

    private ProblemsDTO initializeProblemsDTO(List<TicketStatDTO> stats, PeriodEnum period){

        Map<String, Double> category1mScores = stats.stream()
                .collect(Collectors.groupingBy(TicketStatDTO::getCategory, Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));

        Map<String, Double> category2mScores = stats.stream()
                .collect(Collectors.groupingBy(TicketStatDTO::getCategory,
                        Collectors.summingDouble(ticket -> getUrgencyScore(ticket.getUrgency()))));

        Map<String, Double> category3mScores = stats.stream()
                .collect(Collectors.groupingBy(TicketStatDTO::getCategory,
                        Collectors.averagingDouble(TicketStatDTO::getTimeToRes)));

        String fml = getLeadingCategory(category1mScores);
        String sml = getLeadingCategory(category2mScores);
        String tml = getLeadingCategory(category3mScores);
        String aml = category1mScores.keySet().stream()
                .collect(Collectors.toMap(
                        // Категория
                        category -> category,
                        // Вычисление значения по формуле
                        category -> {
                            double score1 = category1mScores.getOrDefault(category, 0.0) * 0.5;
                            double score2 = category2mScores.getOrDefault(category, 0.0);
                            double score3 = category3mScores.getOrDefault(category, 0.0) * 0.7;
                            return score1 + score2 + score3;
                        }))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        return new ProblemsDTO(fml, sml, tml, aml, period);
    }

    private static double getUrgencyScore(TicketUrgency urgency) {
        return switch (urgency) {
            case Critical -> 1.0;
            case High -> 0.7;
            case Average -> 0.4;
            case Low -> 0.1;
        };
    }

    private String getLeadingCategory(Map<String, Double> map){
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /* ПОДСЧЁТ СТАТИСТИКИ
    суммарная проблемность категории =
                            общее кол-во тикетов данной категории * 0,5
                            + (сумма из:кол-во тикетов определённой urgency * на коэф urgency(1 - critical / 0,7 - high / 0,4 - average  / 0,1 - low) )
                            + среднее время разрешения в днях * 0,7
    частная проблемность категории = одно из трёх вышеприведённых слагаемых

    тир-лист инженеров для разрешения тикета определённой категории:
    общий пул данных по тикетам разбивается на категории
    для каждой категории берутся инженеры, уже принимавшие участие в разрешении хотя бы 1 тикета этой категории (если не принимал - система не сможет оценить пригодность: считать ниже, чем у кого-либо из итогового тир-листа для категории)
    для каждого инженера в рамках категории высчитывается score = кол-во тикетов этой категории, где он assignee * 0,5
                                                                    + сумма из:кол-во тикетов определённой urgency * на коэф urgency(1 - critical / 0,7 - high / 0,4 - average  / 0,1 - low)
                                                                    + среднее время разрешения в днях * 0,7
                                                                    (P.S.: тот же самый подсчёт, что и для общей проблемности категории)
    score умножается на (10 - activeTickets), где activeTickets - количество тикетов определённого инженера In_progress (чем больше нерешённых тикетов - тем менее разумно дёргать инженера и давать ему новый тикет)
    инженеры в рамках категории сортируются по своему score и из списка определяется, кто из них лучше всего подходит для назначения ему нового тикета по этой категории

    ПРИМЕЧАНИЕ: если инженер никогда не бывает в рекомендационном списке ВСЕХ категорий одновременно - значит, он ни за какой тикет не брался никогда вообще: либо новенький, либо гультай :)
     */
}

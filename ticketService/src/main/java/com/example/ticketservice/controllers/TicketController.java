package com.example.ticketservice.controllers;

import com.example.ticketservice.TicketService;
import com.example.ticketservice.kafka.KafkaProducer;
import com.example.ticketservice.models.dtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")

        //Сделать все через REST
        // Перевести все на микросервисы (eureka, gateway и тд)
        // Нотификации вынести в отдельный микросервис и общаться с ним через брокер
        // Базу данных и брокер помещаем в докер компоуз
        // Покрыть тестами (юниты и интеграционные, использовать тест контейнеры)

public class TicketController {
    private final TicketService ticketService;
    private final KafkaProducer kp;

    @GetMapping("/allTickets")
    public ResponseEntity<?> allTicketsOverview() throws InterruptedException {
        return ticketService.getAllowedTickets();
    }

    @PostMapping(value = "/createTicket")
    public ResponseEntity<Integer> createTicket(@RequestParam(name = "newTicket") String newTicketJson){
        return ticketService.createTicket(newTicketJson);
    }

    @GetMapping("/ticketOverview/{id}")
    public ResponseEntity<TicketFullDTO> ticketOverview(@PathVariable Integer id){
        return ResponseEntity.ok().body(ticketService.getTicketFullDTO(id));
    }

    @GetMapping("/editTicket/{id}")
    public ResponseEntity<TicketEditDTO> editTicket(@PathVariable Integer id){
        return ResponseEntity.ok().body(ticketService.getTicketEditDTO(id));
    }

    @PostMapping(value = "/editTicket/{id}")
    public ResponseEntity<Integer> saveEditedTicket(@RequestParam(name = "editTicket") String editedTicketJson) {
        return ticketService.editTicket(editedTicketJson);
    }

    @PostMapping("/transmitStatus/{ticketId}")
    public ResponseEntity<String> transmitStatus(@PathVariable Integer ticketId,
                                                 @RequestBody String status){
        String resp = ticketService.transmitStatus(ticketId, status);
        return ResponseEntity.ok().body("{\"status\":\"" + resp + "\"}");
    }

    @PostMapping(value = "/leaveComment/{id}")
    public ResponseEntity<CommentDTO> leaveComment(@RequestBody String text,
                                                   @PathVariable Integer id){
        return ResponseEntity.ok().body(ticketService.leaveComment(text, id));
    }

    @PostMapping("/leaveFeedback/{ticketId}")
    public ResponseEntity<HistoryDTO> leaveFeedback(@PathVariable Integer ticketId,
                                                    @RequestBody JsonNode json){
        return ResponseEntity.ok().body(ticketService.leaveFeedback(ticketId, json));
    }

    @PostMapping(value = "/addHisRecForAtt")
    public void addHisRecForAtt(@RequestBody List<HistoryAttDTO> hisRecForAtt){
        ticketService.addHisRecForAtt(hisRecForAtt);
    }

    @GetMapping(value = "/getStatDTOs")
    public ResponseEntity<List<TicketStatDTO>> getStatDTOs(){
        return ticketService.getStatDTOs();
    }

    @GetMapping(value = "/kafka")
    public void kafka(){
        kp.sendMessage(new String[]{"array"}, "test", "bodY");
    }
}

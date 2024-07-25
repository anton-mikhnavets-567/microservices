package com.example.ticketservice;

import com.example.ticketservice.kafka.KafkaProducer;
import com.example.ticketservice.models.dtos.*;
import com.example.ticketservice.models.dtos.mappers.*;
import com.example.ticketservice.models.entities.*;
import com.example.ticketservice.models.enums.TicketState;
import com.example.ticketservice.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final HistoryRepository historyRepository;
    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final TicketServiceClient ticketSC;
    private final KafkaProducer kafkaProducer;

    private final TicketDTOMapper ticketDTOMapper;
    private final TicketFullDTOMapper ticketFullDTOMapper;
    private final TicketCreateDTOMapper ticketCreateDTOMapper;
    private final TicketEditDTOMapper ticketEditDTOMapper;
    private final TicketStatDTOMapper ticketStatDTOMapper;
    private final HistoryAttDTOMapper historyAttDTOMapper;
    UserInfoDTO getUserInfoFromContext(){
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String email = token.getTokenAttributes().get("email").toString();
        String role = token.getToken().getClaimAsStringList("spring_sec_roles").stream()
                .filter(r -> r.startsWith("ROLE_"))
                .findFirst()
                .orElse(null);
        String name = token.getTokenAttributes().get("preferred_username").toString();
        Integer id = ticketSC.getUserID(email);
        return new UserInfoDTO(id, role, email, name);
    }

    public ResponseEntity<List<TicketDTO>> getAllowedTickets() throws InterruptedException {
        UserInfoDTO userInfo = getUserInfoFromContext();
        List<Ticket> tickets;
        switch (userInfo.role()) {
            case "ROLE_EMPLOYEE" -> tickets = ticketRepository.getAllTicketsForEmployee(userInfo.id());
            case "ROLE_MANAGER" -> {
                List<Integer> employeeIDs = ticketSC.getAllEmployeeIDs();
                tickets = ticketRepository.getAllTicketsForManager(userInfo.id(), employeeIDs);
            }
            case "ROLE_ENGINEER" -> {
                List<Integer> employeeAndManagerIDs = ticketSC.getAllEmployeeAndManagerIDs();
                tickets = ticketRepository.getAllTicketsForEngineer(userInfo.id(), employeeAndManagerIDs);
            }
            default -> throw new IllegalStateException("Unexpected value: " + userInfo.role());
        }

        Set<Integer> uniqueIDs = tickets.stream()
                .flatMap(ticket -> Stream.of(ticket.getAssigneeID(), ticket.getApproverID(), ticket.getOwnerID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, UserInfoDTO> users = ticketSC.getUserInfoDTOs(uniqueIDs);
        Thread.sleep(1000);
        List<TicketDTO> ticketDTOs = ticketDTOMapper.allToDTOs(users, tickets);
        System.out.println("ticketDTOS: "+ ticketDTOs + "\n"
                + "users: " + users + "\n"
                + "tickets: " + tickets + "\n"
                + "uniqueIDs: " + uniqueIDs );

        return ResponseEntity.ok().body(ticketDTOs);
    }

    public TicketFullDTO getTicketFullDTO(Integer ticketId) {
        Ticket t = ticketRepository.getTicketForOverviewById(ticketId);
        Set<Integer> userIds = Stream.of(t.getAssigneeID(), t.getApproverID(), t.getOwnerID())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, UserInfoDTO> users = ticketSC.getUserInfoDTOs(userIds);
        List<AttachmentDTO> attachments = ticketSC.getAttachmentDTOs(ticketId);
        TicketFullDTO fullDTO = ticketFullDTOMapper.toDTO(t, users.get(t.getOwnerID()), users.get(t.getApproverID()), users.get(t.getAssigneeID()), attachments);
        System.out.println(fullDTO);
        return fullDTO;
    }

    public TicketEditDTO getTicketEditDTO(Integer ticketId) {
        return ticketEditDTOMapper.toDTO(ticketRepository.getTicketForOverviewById(ticketId));
    }

    @Transactional
    public ResponseEntity<Integer> createTicket(String newTicketJson) {
        try {
            TicketCreateDTO dto = objectMapper.readValue(newTicketJson, TicketCreateDTO.class);

            UserInfoDTO userInfo = getUserInfoFromContext();
            Category category = categoryRepository.getCategoryById(dto.getCategoryID());
            Ticket newTicket = ticketCreateDTOMapper.toEntity(dto, category, userInfo.id());

            ticketRepository.save(newTicket);
            additionalTicketProcessing (newTicket.getId(), userInfo.id(), dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(newTicket.getId());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public void additionalTicketProcessing (Integer ticketId, Integer userID, TicketCreateDTO dto){

        if (dto.getComment()!=null && !dto.getComment().isEmpty() ){
            Comment comment = new Comment(userID, dto.getComment(), ticketId);
            commentRepository.save(comment);
        }

        History crRecord = new History(ticketId,
                "Заявка создана",
                userID,
                "Заявка создана");
        historyRepository.save(crRecord);

        String[] managers = ticketSC.getEmailsByRole("Manager");
        kafkaProducer.sendMessage(managers,
                "Новая заявка",
                "Уважаемые мееджеры,<br><br>" + "Новая заявка " + "<a href=\"http://localhost:4200/ticketOverview/" + ticketId + "\">" + ticketId + "</a>" + " ожидает подтверждения");
    }

    @Transactional
    public ResponseEntity<Integer> editTicket(String editedTicketJson) {
        try {
            TicketEditDTO dto = objectMapper.readValue(editedTicketJson, TicketEditDTO.class);
            Ticket newTicket = ticketEditDTOMapper.toEntity(dto);
            Ticket oldTicket = ticketRepository.getTicketForOverviewById(dto.getId());
            BeanUtils.copyProperties(oldTicket, newTicket, "category","id", "name", "description","urgency","desiredResolutionDate");
            newTicket.setCategory(categoryRepository.getCategoryById(dto.getCategoryId()));

            UserInfoDTO userInfo = getUserInfoFromContext();

            History record = new History(newTicket.getId(),
                                        "Заявка изменена",
                                        userInfo.id(),
                                        "Заявка изменена");
            historyRepository.save(record);
            ticketRepository.save(newTicket);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public CommentDTO leaveComment(String text, Integer ticketId) {
        UserInfoDTO userInfo = getUserInfoFromContext();
        Comment comment = new Comment(userInfo.id(), text, ticketId);
        commentRepository.save(comment);
        return new CommentDTO(userInfo.username(), text, LocalDateTime.now());
    }
    @Transactional
    public void addHisRecForAtt(List<HistoryAttDTO> dtos) {
        Integer id = getUserInfoFromContext().id();
        List<History> histories = historyAttDTOMapper.allToEntity(dtos, id);
        historyRepository.saveAll(histories);
    }

    @Transactional
    public String transmitStatus(Integer ticketId, String action) {
        Ticket ticket = ticketRepository.getTicketForOverviewById(ticketId);
        String previousStatus = String.valueOf(ticket.getState());
        UserInfoDTO userInfo = getUserInfoFromContext();
        String selectedAction = action.substring(1, action.length() - 1);
        String ticketLink = "<a href=\"http://localhost:4200/ticketOverview/" + ticketId + "\">" + ticketId + "</a>";

        switch (selectedAction) {
            case "Submit" -> {
                ticket.setState(TicketState.New);
                kafkaProducer.sendMessage(ticketSC.getEmailsByRole("Manager"),
                        "Новая заявка",
                        "Уважаемые менеджеры,<br><br>" + "Новая заявка " + ticketLink + " ожидает подтверждения");
            }
            case "Approve" -> {
                ticket.setState(TicketState.Approved);
                ticket.setApproverID(userInfo.id());

                kafkaProducer.sendMessage(ticketSC.getEmailsByRole("Engineer"),
                        "Заявка одобрена",
                        "Уважаемые инженеры,<br><br>" + "Заявка " + ticketLink + " одобрена менеджером"
                );
                kafkaProducer.sendMessage(ticketSC.getEmailByID(ticket.getOwnerID()),
                        "Заявка одобрена",
                        "Уважаемый пользователь,<br><br>" + "Ваша заявка " + ticketLink + " одобрена менеджером"
                );
            }
            case "Decline" -> {
                ticket.setState(TicketState.Declined);
                ticket.setApproverID(userInfo.id());

                kafkaProducer.sendMessage(ticketSC.getEmailByID(ticket.getOwnerID()),
                        "Заявка отклонена",
                        "Уважаемый пользователь," + "<br><br>" + "Ваша заявка " + ticketLink + " отклонена менеджером"
                );
            }
            case "Cancel" -> {
                ticket.setState(TicketState.Cancelled);
                ticket.setAssigneeID(userInfo.id());

                kafkaProducer.sendMessage(new String[]{ticketSC.getEmailByID(ticket.getOwnerID()), userInfo.email()},
                        "Заявка отменена",
                        "Уважаемый пользователь," + "<br><br>" + "Ваша заявка " + ticketLink + " отменена инженером"
                );
            }
            case "Assign to me" -> {
                ticket.setState(TicketState.In_progress);
                ticket.setAssigneeID(userInfo.id());
            }
            case "Done" -> {
                ticket.setState(TicketState.Done);
                kafkaProducer.sendMessage(ticketSC.getEmailByID(ticket.getOwnerID()),
                        "Заявка выполнена",
                        "Уважаемый пользователь," + "<br><br>" + "Ваша заявка " + ticketLink + " была выполнена"
                );
            }
        }
        History record = new History(ticketId,
                "Статус заявки изменён",
                userInfo.id(),
                "Статус заявки изменён с '" + previousStatus + "' на '" + ticket.getState().toString() + "'");
        ticketRepository.save(ticket);
        historyRepository.save(record);
        return ticket.getState().toString();
    }
    @Transactional
    public HistoryDTO leaveFeedback(Integer ticketId, JsonNode json) {
        String commentText = json.get("commentText").asText();
        Integer rate = json.get("starRating").asInt();
        String assigneeEmail = json.get("assigneeEmail").asText();
        Optional<Feedback> existingFeedback = feedbackRepository.getFeedbackByTicketId(ticketId);
        UserInfoDTO userInfo = getUserInfoFromContext();

        if (existingFeedback.isPresent()) {
            Feedback feedback = existingFeedback.get();
            feedback.setRate(rate);
            feedback.setText(commentText);
            feedback.setTimestamp(LocalDateTime.now());
            feedbackRepository.save(feedback);
        } else {
            Feedback newFeedback = new Feedback(userInfo.id(), rate, commentText, ticketId);
            feedbackRepository.save(newFeedback);
        }
        String commentAddition = null;
        if (!commentText.isBlank()){
            commentAddition = "с комментарием: " + commentText;
        }
        History record = new History(ticketId,
                "Выполнение заявки оценено!",
                userInfo.id(),
                "Выполнение заявки оценено " + "★".repeat(rate) + Objects.toString(commentAddition, " "));

        kafkaProducer.sendMessage(assigneeEmail,
                "Новый отзыв!",
                "Уважаемый инженер," + "<br><br>" + "Был предоставлен отзыв на выполнение заявки номер " + "<a href=\"http://localhost:4200/ticketOverview/" + ticketId + "\">" + ticketId + "</a>"
        );

        historyRepository.save(record);
        return new HistoryDTO(LocalDateTime.now(), record.getAction(), userInfo.username(), record.getDescription());
    }

    public ResponseEntity<List<TicketStatDTO>> getStatDTOs() {
        List<Ticket> tickets = ticketRepository.getAllTickets();
        List<UserInfoDTO> assignees = ticketSC.getAllEngineers();
        return ResponseEntity.ok().body(ticketStatDTOMapper.allToDTO(tickets, assignees));
    }
}

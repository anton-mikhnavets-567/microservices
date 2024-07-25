package com.example.ticketservice;

import com.example.ticketservice.kafka.KafkaProducer;
import com.example.ticketservice.models.dtos.TicketDTO;
import com.example.ticketservice.models.dtos.UserInfoDTO;
import com.example.ticketservice.models.dtos.mappers.*;
import com.example.ticketservice.models.entities.Category;
import com.example.ticketservice.models.entities.Ticket;
import com.example.ticketservice.models.enums.TicketState;
import com.example.ticketservice.models.enums.TicketUrgency;
import com.example.ticketservice.models.enums.UserRole;
import com.example.ticketservice.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private TicketServiceClient ticketSC;
    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private TicketDTOMapper ticketDTOMapper;
    @Mock
    private TicketFullDTOMapper ticketFullDTOMapper;
    @Mock
    private TicketCreateDTOMapper ticketCreateDTOMapper;
    @Mock
    private TicketEditDTOMapper ticketEditDTOMapper;
    @Mock
    private TicketStatDTOMapper ticketStatDTOMapper;
    @Mock
    private HistoryAttDTOMapper historyAttDTOMapper;
    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        // Сброс всех моков после каждого теста
        Mockito.reset(ticketRepository, categoryRepository, historyRepository, commentRepository, feedbackRepository, ticketSC, kafkaProducer,
                ticketDTOMapper, ticketFullDTOMapper, ticketCreateDTOMapper, ticketEditDTOMapper, ticketStatDTOMapper, historyAttDTOMapper);
        SecurityContextHolder.clearContext();
        System.out.println("SECURITY: " + SecurityContextHolder.getContext().toString());
    }

    private void setSecurityContextForSpecificRole(String role) {
        Map<String, Object> tokenAttributes = new HashMap<>();
        tokenAttributes.put("email", "test@example.com");
        tokenAttributes.put("preferred_username", "testUser");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsStringList("spring_sec_roles")).thenReturn(List.of(role, "some_authority1", "some_authority2"));

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getToken()).thenReturn(jwt);
        when(token.getTokenAttributes()).thenReturn(tokenAttributes);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(token);
        SecurityContextHolder.setContext(context);
    }


    @Test
    public void getUserInfoFromContext_shouldReturnUserInfoDTO(){
        System.out.println("===========TESTING CONTEXT=============");
        setSecurityContextForSpecificRole("ROLE_MANAGER");

        when(ticketSC.getUserID("test@example.com")).thenReturn(123);

        UserInfoDTO userInfo = ticketService.getUserInfoFromContext();

        assertEquals(123, userInfo.id());
        assertEquals("ROLE_MANAGER", userInfo.role());
        assertEquals("test@example.com", userInfo.email());
        assertEquals("testUser", userInfo.username());

        verify(ticketSC).getUserID("test@example.com");

        System.out.println("===========TESTING CONTEXT ENDED=============");
    }

    @Test
    public void testGetAllowedTicketsForEmployee() throws InterruptedException {
        setSecurityContextForSpecificRole("ROLE_EMPLOYEE");
        when(ticketSC.getUserID("test@example.com")).thenReturn(1);

        System.out.println("===========TESTING FOR EMPLOYEE=============");

        List<Ticket> tickets = Arrays.asList(
                new Ticket(1, "Ticket1", "Descr1", LocalDate.of(2024, 2, 23), LocalDate.of(2024, 2, 26), null, 1, TicketState.New, new Category(1, "1Category"), TicketUrgency.Low, null, null, null, null),
                new Ticket(2, "Ticket2", "Descr2", LocalDate.of(2024, 2, 24), LocalDate.of(2024, 2, 28), null, 1, TicketState.Approved, new Category(2, "2Category"), TicketUrgency.Low, 3, null, null, null),
                new Ticket(3, "Ticket3", "Descr3", LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 27), 5, 1, TicketState.In_progress, new Category(3, "3Category"), TicketUrgency.Average, 3, null, null, null)
        );
        when(ticketRepository.getAllTicketsForEmployee(anyInt())).thenReturn(tickets);

        Set<Integer> uniqueIDs = Set.of(1, 3, 5);

        Map<Integer, UserInfoDTO> users = Map.of(
                1, new UserInfoDTO(1, "ROLE_EMPLOYEE", "employee@example.com", "testEmployee"),
                3, new UserInfoDTO(3, "ROLE_MANAGER", "manager@example.com", "testManager"),
                5, new UserInfoDTO(5, "ROLE_ENGINEER", "engineer@example.com", "testEngineer")
        );

        System.out.println("TEST MAP: " + users);
        System.out.println("TEST TICKETS: " + tickets);
        lenient().doReturn(users).when(ticketSC).getUserInfoDTOs(uniqueIDs);

        List<TicketDTO> ticketDTOs = Arrays.asList(
                new TicketDTO(1, "Ticket1", "1Category", LocalDate.of(2024, 2, 23), LocalDate.of(2024, 2, 26), "employee@example.com", UserRole.Employee, null, null, TicketState.New, TicketUrgency.Low),
                new TicketDTO(2, "Ticket2", "2Category", LocalDate.of(2024, 2, 24), LocalDate.of(2024, 2, 28), "employee@example.com", UserRole.Employee, "manager@example.com", null, TicketState.Approved, TicketUrgency.Low),
                new TicketDTO(3, "Ticket3", "3Category", LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 27), "employee@example.com", UserRole.Employee, "manager@example.com", "engineer@example.com", TicketState.In_progress, TicketUrgency.Average)
        );
        when(ticketDTOMapper.allToDTOs(users, tickets)).thenReturn(ticketDTOs);

        // Выполнение тестируемого метода
        ResponseEntity<List<TicketDTO>> response = ticketService.getAllowedTickets();
        // Проверка результата
        assertEquals(ResponseEntity.ok().body(ticketDTOs), response);
        verify(ticketDTOMapper).allToDTOs(users, tickets);
        verify(ticketRepository).getAllTicketsForEmployee(1);
        verify(ticketSC).getUserInfoDTOs(uniqueIDs);
        System.out.println("===========TESTING FOR EMPLOYEE ENDED=============");
    }

    @Test
    public void testGetAllowedTicketsForManager() throws InterruptedException {
        setSecurityContextForSpecificRole("ROLE_MANAGER");
        when(ticketSC.getUserID("test@example.com")).thenReturn(3);

        System.out.println("===========TESTING FOR MANAGER=============");

        List<Ticket> tickets = Arrays.asList(
                new Ticket(1, "Ticket1", "Descr1", LocalDate.of(2024, 2, 23), LocalDate.of(2024, 2, 26), null, 1, TicketState.New, new Category(1, "1Category"), TicketUrgency.Low, null, null, null, null),
                new Ticket(2, "Ticket2", "Descr2", LocalDate.of(2024, 2, 24), LocalDate.of(2024, 2, 28), null, 1, TicketState.Approved, new Category(2, "2Category"), TicketUrgency.Low, 3, null, null, null),
                new Ticket(3, "Ticket3", "Descr3", LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 27), 5, 1, TicketState.In_progress, new Category(3, "3Category"), TicketUrgency.Average, 3, null, null, null)
        );
        when(ticketSC.getAllEmployeeIDs()).thenReturn(List.of(1, 2));
        when(ticketRepository.getAllTicketsForManager(3,List.of(1, 2))).thenReturn(tickets);

        Set<Integer> uniqueIDs = Set.of(1, 3, 5);

        Map<Integer, UserInfoDTO> users = Map.of(
                1, new UserInfoDTO(1, "ROLE_EMPLOYEE", "employee@example.com", "testEmployee"),
                3, new UserInfoDTO(3, "ROLE_MANAGER", "manager@example.com", "testManager"),
                5, new UserInfoDTO(5, "ROLE_ENGINEER", "engineer@example.com", "testEngineer")
        );

        System.out.println("TEST MAP: " + users);
        System.out.println("TEST TICKETS: " + tickets);
        lenient().doReturn(users).when(ticketSC).getUserInfoDTOs(uniqueIDs);

        List<TicketDTO> ticketDTOs = Arrays.asList(
                new TicketDTO(1, "Ticket1", "1Category", LocalDate.of(2024, 2, 23), LocalDate.of(2024, 2, 26), "employee@example.com", UserRole.Employee, null, null, TicketState.New, TicketUrgency.Low),
                new TicketDTO(2, "Ticket2", "2Category", LocalDate.of(2024, 2, 24), LocalDate.of(2024, 2, 28), "employee@example.com", UserRole.Employee, "manager@example.com", null, TicketState.Approved, TicketUrgency.Low),
                new TicketDTO(3, "Ticket3", "3Category", LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 27), "employee@example.com", UserRole.Employee, "manager@example.com", "engineer@example.com", TicketState.In_progress, TicketUrgency.Average)
        );
        when(ticketDTOMapper.allToDTOs(users, tickets)).thenReturn(ticketDTOs);

        // Выполнение тестируемого метода
        ResponseEntity<List<TicketDTO>> response = ticketService.getAllowedTickets();
        // Проверка результата
        assertEquals(ResponseEntity.ok().body(ticketDTOs), response);
        verify(ticketDTOMapper).allToDTOs(users, tickets);
        verify(ticketRepository).getAllTicketsForManager(3, List.of(1, 2));
        verify(ticketSC).getUserInfoDTOs(uniqueIDs);

        System.out.println("===========TESTING FOR MANAGER ENDED=============");
    }

    @Test
    public void testGetAllowedTicketsForEngineer() throws InterruptedException {
        setSecurityContextForSpecificRole("ROLE_ENGINEER");
        when(ticketSC.getUserID("test@example.com")).thenReturn(5);

        System.out.println("===========TESTING FOR ENGINEER=============");

        List<Ticket> tickets = Arrays.asList(
                new Ticket(1, "Ticket1", "Descr1", LocalDate.of(2024, 2, 23), LocalDate.of(2024, 2, 26), null, 1, TicketState.New, new Category(1, "1Category"), TicketUrgency.Low, null, null, null, null),
                new Ticket(2, "Ticket2", "Descr2", LocalDate.of(2024, 2, 24), LocalDate.of(2024, 2, 28), null, 1, TicketState.Approved, new Category(2, "2Category"), TicketUrgency.Low, 3, null, null, null),
                new Ticket(3, "Ticket3", "Descr3", LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 27), 5, 1, TicketState.In_progress, new Category(3, "3Category"), TicketUrgency.Average, 3, null, null, null)
        );
        when(ticketSC.getAllEmployeeAndManagerIDs()).thenReturn(List.of(1, 2, 3, 4));
        when(ticketRepository.getAllTicketsForEngineer(5,List.of(1, 2, 3, 4))).thenReturn(tickets);

        Set<Integer> uniqueIDs = Set.of(1, 3, 5);

        Map<Integer, UserInfoDTO> users = Map.of(
                1, new UserInfoDTO(1, "ROLE_EMPLOYEE", "employee@example.com", "testEmployee"),
                3, new UserInfoDTO(3, "ROLE_MANAGER", "manager@example.com", "testManager"),
                5, new UserInfoDTO(5, "ROLE_ENGINEER", "engineer@example.com", "testEngineer")
        );

        System.out.println("TEST MAP: " + users);
        System.out.println("TEST TICKETS: " + tickets);
        lenient().doReturn(users).when(ticketSC).getUserInfoDTOs(uniqueIDs);

        List<TicketDTO> ticketDTOs = Arrays.asList(
                new TicketDTO(1, "Ticket1", "1Category", LocalDate.of(2024, 2, 23), LocalDate.of(2024, 2, 26), "employee@example.com", UserRole.Employee, null, null, TicketState.New, TicketUrgency.Low),
                new TicketDTO(2, "Ticket2", "2Category", LocalDate.of(2024, 2, 24), LocalDate.of(2024, 2, 28), "employee@example.com", UserRole.Employee, "manager@example.com", null, TicketState.Approved, TicketUrgency.Low),
                new TicketDTO(3, "Ticket3", "3Category", LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 27), "employee@example.com", UserRole.Employee, "manager@example.com", "engineer@example.com", TicketState.In_progress, TicketUrgency.Average)
        );
        when(ticketDTOMapper.allToDTOs(users, tickets)).thenReturn(ticketDTOs);

        // Выполнение тестируемого метода
        ResponseEntity<List<TicketDTO>> response = ticketService.getAllowedTickets();
        // Проверка результата
        assertEquals(ResponseEntity.ok().body(ticketDTOs), response);
        verify(ticketDTOMapper).allToDTOs(users, tickets);
        verify(ticketRepository).getAllTicketsForEngineer(5, List.of(1, 2, 3, 4));
        verify(ticketSC).getUserInfoDTOs(uniqueIDs);

        System.out.println("===========TESTING FOR ENGINEER ENDED=============");
    }
}

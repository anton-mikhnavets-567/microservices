package com.example.ticketservice.models.entities;

import com.example.ticketservice.models.enums.TicketState;
import com.example.ticketservice.models.enums.TicketUrgency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "ticket", schema =  "public")
public class Ticket {
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "ticket_id_seq", sequenceName = "ticket_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_id_seq")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_on")
    private LocalDate createdOn = LocalDate.now();

    @Column(name = "desired_resolution_date")
    private LocalDate desiredResolutionDate;

    @Column(name = "assignee_id")
    private Integer assigneeID;

    @Column(name = "owner_id")
    private Integer ownerID;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", columnDefinition = "ticketState")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TicketState state = TicketState.New;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", columnDefinition = "ticketUrgency")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TicketUrgency urgency;

    @Column(name = "approver_id")
    private Integer approverID;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ticket_id")
    private List<Comment> comments;

//    @JsonIgnore
//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "ticket_id")
//    private List<Attachment> attachments;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ticket_id")
    private List<History> historyRecords;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ticket_id")
    private List<Feedback> feedbacks;
}

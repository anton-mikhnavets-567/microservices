package com.example.attachmentservice.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "history", schema =  "public")
public class History {

    public History(Integer ticketId, String action, Integer userID, String description){
        this.ticketId = ticketId;
        this.action = action;
        this.userID = userID;
        this.description = description;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ticket_id")
    private Integer ticketId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "action")
    private String action;

    @Column(name = "user_id")
    private Integer userID;

    @Column(name = "description")
    private String description;
}

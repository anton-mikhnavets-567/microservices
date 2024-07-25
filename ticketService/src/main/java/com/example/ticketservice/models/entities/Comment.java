package com.example.ticketservice.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "comment", schema =  "public")
public class Comment {
    public Comment(Integer userID, String text, Integer ticketId){
        this.userID = userID;
        this.text = text;
        this.ticketId = ticketId;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userID;

    @Column(name = "text")
    private String text;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "ticket_id")
    private Integer ticketId;
}

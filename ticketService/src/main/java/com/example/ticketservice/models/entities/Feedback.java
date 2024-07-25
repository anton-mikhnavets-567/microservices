package com.example.ticketservice.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "feedback", schema =  "public")
public class Feedback {

    public Feedback(Integer userId, Integer rate, String text, Integer ticketId){
        this.userId = userId;
        this.rate = rate;
        this.text = text;
        this.ticketId = ticketId;
    }
    @Id
    @Column(name ="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "rate")
    private Integer rate;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "text")
    private String text;

    @Column(name = "ticket_id")
    private Integer ticketId;
}

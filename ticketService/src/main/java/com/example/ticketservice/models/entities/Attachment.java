package com.example.ticketservice.models.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "attachment", schema =  "public")
public class Attachment {
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "attachment_id_seq", sequenceName = "attachment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_id_seq")
    private Integer id;

    @Column(name = "contents")
    private byte[] contents;

    @Column(name = "ticket_id")
    private Integer ticketId;

    @Column(name = "name")
    private String name;
}

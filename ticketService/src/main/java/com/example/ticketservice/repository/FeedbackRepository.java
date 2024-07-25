package com.example.ticketservice.repository;

import com.example.ticketservice.models.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    @Query(nativeQuery = true, value="select * from feedback f where f.ticket_id = :ticketId")
    Optional<Feedback> getFeedbackByTicketId(@Param("ticketId") Integer ticketId);
}

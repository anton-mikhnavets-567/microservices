package com.example.ticketservice.repository;

import com.example.ticketservice.models.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}

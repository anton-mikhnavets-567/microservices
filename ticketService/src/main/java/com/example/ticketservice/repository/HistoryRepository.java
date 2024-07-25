package com.example.ticketservice.repository;

import com.example.ticketservice.models.entities.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Integer> {
}

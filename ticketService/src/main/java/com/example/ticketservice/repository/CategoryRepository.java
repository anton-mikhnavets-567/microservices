package com.example.ticketservice.repository;

import com.example.ticketservice.models.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("select c from Category c where c.id = :id")
    public Category getCategoryById(@Param("id") Integer id);
}

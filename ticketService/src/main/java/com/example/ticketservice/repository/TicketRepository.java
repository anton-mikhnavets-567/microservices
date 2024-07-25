package com.example.ticketservice.repository;

import com.example.ticketservice.models.entities.Ticket;
import com.example.ticketservice.models.enums.TicketState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TicketRepository extends JpaRepository <Ticket, Integer> {

    @Query("SELECT t FROM Ticket t " +
            "left join fetch t.category c " +
            "where t.ownerID = :ownerID")
    List<Ticket> getAllTicketsForEmployee(@Param("ownerID") Integer ownerID);

    @Query("SELECT t FROM Ticket t " +
            "left join fetch t.category c " +
            "WHERE (t.ownerID = :managerID) " +
            "OR ((t.ownerID IN :employeeIDs) AND (t.state = 'New')) " +
            "OR ((t.approverID = :managerID) AND (t.state IN ('Approved', 'Declined', 'Cancelled', 'In_progress', 'Done')))" +
            "ORDER BY t.urgency, t.desiredResolutionDate")
    List<Ticket> getAllTicketsForManager(@Param("managerID") Integer managerID,
                                         @Param("employeeIDs") List<Integer> employeeIDs);

    @Query("SELECT t FROM Ticket t " +
            "left join fetch t.category c " +
            "WHERE ((t.ownerID IN :employeeManagerIDs AND (t.state = 'Approved')) " +
            "OR ((t.assigneeID = :engineerID) AND (t.state in ('In_progress', 'Done'))))" +
            "ORDER BY t.urgency, t.desiredResolutionDate")
    List<Ticket> getAllTicketsForEngineer(@Param("engineerID") Integer engineerID,
                                          @Param("employeeManagerIDs") List<Integer> employeeManagerIDs);

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.id = :ticketID")
    Ticket getTicketForOverviewById(@Param ("ticketID") Integer ticketID);
    @Query("SELECT t FROM Ticket t left join fetch t.category left join fetch t.historyRecords")
    List<Ticket> getAllTickets();
}


package com.fieldservice.repository;

import com.fieldservice.domain.enums.Priority;
import com.fieldservice.domain.enums.TicketStatus;
import com.fieldservice.domain.model.Technician;
import com.fieldservice.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByNumber(String number);

    List<Ticket> findByTechnicianOrderByCreatedAtDesc(Technician technician);

    List<Ticket> findByTechnicianAndStatusInOrderByCreatedAtDesc(Technician technician, Collection<TicketStatus> statuses);

    long countByTechnicianAndStatusIn(Technician technician, Collection<TicketStatus> statuses);

    long countByTechnician(Technician technician);

    /**
     * RN006: Destaque de chamados HIGH e CRITICAL ainda não concluídos para o técnico,
     * ordenados por prioridade (CRITICAL primeiro, depois HIGH) e data mais recente.
     */
    @Query("""
        SELECT t FROM Ticket t
        WHERE t.technician = :technician
          AND t.priority IN (:priorities)
          AND t.status NOT IN (:terminalStatuses)
        ORDER BY
          CASE t.priority
            WHEN com.fieldservice.domain.enums.Priority.CRITICAL THEN 1
            WHEN com.fieldservice.domain.enums.Priority.HIGH THEN 2
            WHEN com.fieldservice.domain.enums.Priority.MEDIUM THEN 3
            WHEN com.fieldservice.domain.enums.Priority.LOW THEN 4
            ELSE 5
          END ASC,
          t.createdAt DESC
    """)
    List<Ticket> findPriorityTickets(
        @Param("technician") Technician technician,
        @Param("priorities") Collection<Priority> priorities,
        @Param("terminalStatuses") Collection<TicketStatus> terminalStatuses
    );
}

package com.flowpay.backend.repository;

import com.flowpay.backend.domain.Team;
import com.flowpay.backend.domain.Ticket;
import com.flowpay.backend.domain.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT t FROM Ticket t WHERE t.team = :team AND t.status = 'WAITING' ORDER BY t.createdAt ASC")
    List<Ticket> findOldestWaitingTicketsWithLock(@Param("team") Team team, Pageable pageable);
    
    long countByAttendantIdAndStatus(UUID attendantId, TicketStatus status);
    
    long countByStatus(TicketStatus status);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findTop10ByStatusOrderByFinishedAtDesc(TicketStatus status);
}

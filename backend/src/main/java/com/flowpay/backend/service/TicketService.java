package com.flowpay.backend.service;

import com.flowpay.backend.domain.Team;
import com.flowpay.backend.domain.Ticket;
import com.flowpay.backend.domain.TicketStatus;
import com.flowpay.backend.dto.TicketCreateRequest;
import com.flowpay.backend.dto.TicketResponse;
import com.flowpay.backend.dto.AttendantResponse;
import com.flowpay.backend.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TeamRoutingStrategy routingStrategy;
    private final TicketDispatcherService dispatcherService;
    private final EventPublisher eventPublisher;

    public TicketService(TicketRepository ticketRepository, TeamRoutingStrategy routingStrategy, TicketDispatcherService dispatcherService, EventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.routingStrategy = routingStrategy;
        this.dispatcherService = dispatcherService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest req) {
        Team team = routingStrategy.determineTeam(req.subject());
        Ticket ticket = Ticket.builder()
            .clientName(req.clientName())
            .subject(req.subject())
            .description(req.description())
            .team(team)
            .build();
        ticket = ticketRepository.save(ticket);
        
        dispatcherService.dispatchNext(team);
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishDashboardUpdate("UPDATE");
            }
        });
        
        return mapToResponse(ticketRepository.findById(ticket.getId()).orElseThrow());
    }

    @Transactional
    public TicketResponse finishTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
            
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket is not IN_PROGRESS");
        }
        
        ticket.setStatus(TicketStatus.FINISHED);
        ticket.setFinishedAt(Instant.now());
        ticketRepository.save(ticket);
        
        dispatcherService.dispatchNext(ticket.getTeam());
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishDashboardUpdate("UPDATE");
            }
        });
        
        return mapToResponse(ticket);
    }
    
    public List<TicketResponse> getActiveTickets() {
        return ticketRepository.findAll().stream()
            .filter(t -> t.getStatus() != TicketStatus.FINISHED)
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .map(this::mapToResponse)
            .toList();
    }
    
    public com.flowpay.backend.dto.DashboardStats getGlobalStats() {
        long inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long waiting = ticketRepository.countByStatus(TicketStatus.WAITING);
        long finished = ticketRepository.countByStatus(TicketStatus.FINISHED);
        
        List<Ticket> waitingTicketsList = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.WAITING)
                .toList();
                
        double avgWaitSeconds = 0;
        if (!waitingTicketsList.isEmpty()) {
            avgWaitSeconds = waitingTicketsList.stream()
                .mapToDouble(t -> java.time.Duration.between(t.getCreatedAt(), Instant.now()).toMillis() / 1000.0)
                .average()
                .orElse(0.0);
        }
        
        List<Ticket> startedTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStartedAt() != null)
                .toList();
                
        double slaResponseSeconds = 0;
        if (!startedTickets.isEmpty()) {
            slaResponseSeconds = startedTickets.stream()
                .mapToDouble(t -> java.time.Duration.between(t.getCreatedAt(), t.getStartedAt()).toMillis() / 1000.0)
                .average()
                .orElse(0.0);
        }
        
        List<TicketResponse> last10Finished = ticketRepository.findTop10ByStatusOrderByFinishedAtDesc(TicketStatus.FINISHED)
                .stream().map(this::mapToResponse).toList();
                
        return new com.flowpay.backend.dto.DashboardStats(inProgress, waiting, finished, avgWaitSeconds, slaResponseSeconds, last10Finished);
    }
    
    private TicketResponse mapToResponse(Ticket t) {
        AttendantResponse ar = null;
        if (t.getAttendant() != null) {
            ar = new AttendantResponse(t.getAttendant().getId(), t.getAttendant().getName(), t.getAttendant().getTeam(), 0);
        }
        return new TicketResponse(t.getId(), t.getClientName(), t.getSubject(), t.getDescription(), t.getTeam(), t.getStatus(), ar, t.getCreatedAt(), t.getStartedAt(), t.getFinishedAt());
    }
}

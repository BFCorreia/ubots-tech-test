package com.flowpay.backend.service;

import com.flowpay.backend.domain.Attendant;
import com.flowpay.backend.dto.AttendantCreateRequest;
import com.flowpay.backend.dto.AttendantResponse;
import com.flowpay.backend.repository.AttendantRepository;
import com.flowpay.backend.repository.TicketRepository;
import com.flowpay.backend.domain.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;

@Service
public class AttendantService {
    
    private final AttendantRepository attendantRepository;
    private final TicketRepository ticketRepository;
    private final EventPublisher eventPublisher;
    private final TicketDispatcherService dispatcherService;

    public AttendantService(AttendantRepository attendantRepository, TicketRepository ticketRepository, EventPublisher eventPublisher, TicketDispatcherService dispatcherService) {
        this.attendantRepository = attendantRepository;
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
        this.dispatcherService = dispatcherService;
    }

    @Transactional
    public AttendantResponse createAttendant(AttendantCreateRequest req) {
        Attendant attendant = Attendant.builder()
            .name(req.name())
            .team(req.team())
            .build();
        attendant = attendantRepository.save(attendant);
        
        for (int i = 0; i < 3; i++) {
            dispatcherService.dispatchNext(req.team());
        }

        AttendantResponse response = mapToResponse(attendant);
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishDashboardUpdate("UPDATE");
            }
        });
        
        return response;
    }
    
    public List<AttendantResponse> getAllAttendants() {
        return attendantRepository.findAll().stream().map(this::mapToResponse).toList();
    }
    
    private AttendantResponse mapToResponse(Attendant a) {
        long active = ticketRepository.countByAttendantIdAndStatus(a.getId(), TicketStatus.IN_PROGRESS);
        return new AttendantResponse(a.getId(), a.getName(), a.getTeam(), active);
    }
}

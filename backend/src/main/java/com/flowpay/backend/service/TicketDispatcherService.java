package com.flowpay.backend.service;

import com.flowpay.backend.domain.Attendant;
import com.flowpay.backend.domain.Team;
import com.flowpay.backend.domain.Ticket;
import com.flowpay.backend.domain.TicketStatus;
import com.flowpay.backend.repository.AttendantRepository;
import com.flowpay.backend.repository.TicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class TicketDispatcherService {

    private final TicketRepository ticketRepository;
    private final AttendantRepository attendantRepository;

    public TicketDispatcherService(TicketRepository ticketRepository, AttendantRepository attendantRepository) {
        this.ticketRepository = ticketRepository;
        this.attendantRepository = attendantRepository;
    }

    @Transactional
    public void dispatchNext(Team team) {
        List<Ticket> waitingTickets = ticketRepository.findOldestWaitingTicketsWithLock(team, PageRequest.of(0, 1));
        if (waitingTickets.isEmpty()) {
            return;
        }

        List<Attendant> availableAttendants = attendantRepository.findAvailableAttendantsWithLock(team, PageRequest.of(0, 1));
        if (availableAttendants.isEmpty()) {
            return;
        }

        Ticket ticket = waitingTickets.get(0);
        Attendant attendant = availableAttendants.get(0);

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAttendant(attendant);
        ticket.setStartedAt(Instant.now());
        ticketRepository.save(ticket);
    }
}

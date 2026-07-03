package com.flowpay.backend.controller;

import com.flowpay.backend.dto.TicketCreateRequest;
import com.flowpay.backend.dto.TicketResponse;
import com.flowpay.backend.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@RequestBody @Valid TicketCreateRequest req) {
        return ticketService.createTicket(req);
    }

    @PostMapping("/{id}/finish")
    public TicketResponse finishTicket(@PathVariable UUID id) {
        return ticketService.finishTicket(id);
    }

    @GetMapping("/active")
    public List<TicketResponse> getActiveTickets() {
        return ticketService.getActiveTickets();
    }
}

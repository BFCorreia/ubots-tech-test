package com.flowpay.backend.controller;

import com.flowpay.backend.dto.DashboardResponse;
import com.flowpay.backend.service.AttendantService;
import com.flowpay.backend.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AttendantService attendantService;
    private final TicketService ticketService;

    public DashboardController(AttendantService attendantService, TicketService ticketService) {
        this.attendantService = attendantService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public DashboardResponse getDashboardMetrics() {
        return new DashboardResponse(
            attendantService.getAllAttendants(),
            ticketService.getActiveTickets(),
            ticketService.getGlobalStats()
        );
    }
}

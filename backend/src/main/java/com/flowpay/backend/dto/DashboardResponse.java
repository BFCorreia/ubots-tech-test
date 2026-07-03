package com.flowpay.backend.dto;

import java.util.List;

public record DashboardResponse(
    List<AttendantResponse> attendants,
    List<TicketResponse> tickets,
    DashboardStats stats
) {}

package com.flowpay.backend.dto;

import java.util.List;

public record DashboardStats(
    long totalInProgress,
    long totalWaiting,
    long totalFinished,
    double avgWaitTimeSeconds,
    double slaResponseTimeSeconds,
    List<TicketResponse> recentFinished
) {}

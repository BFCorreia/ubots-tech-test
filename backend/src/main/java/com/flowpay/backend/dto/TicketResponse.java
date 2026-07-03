package com.flowpay.backend.dto;

import com.flowpay.backend.domain.Team;
import com.flowpay.backend.domain.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String clientName,
    String subject,
    String description,
    Team team,
    TicketStatus status,
    AttendantResponse attendant,
    Instant createdAt,
    Instant startedAt,
    Instant finishedAt
) {}

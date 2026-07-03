package com.flowpay.backend.dto;

import com.flowpay.backend.domain.Team;
import java.util.UUID;

public record AttendantResponse(
    UUID id,
    String name,
    Team team,
    long activeTickets
) {}

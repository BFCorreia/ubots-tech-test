package com.flowpay.backend.dto;

import com.flowpay.backend.domain.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttendantCreateRequest(
    @NotBlank String name,
    @NotNull Team team
) {}
